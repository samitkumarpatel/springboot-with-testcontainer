package com.example.springboot_with_testcontainer;

import com.example.springboot_with_testcontainer.model.Transaction;
import com.example.springboot_with_testcontainer.temporal.TransferWorkflow;
import com.example.springboot_with_testcontainer.temporal.TransferWorkflowImpl;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SpringBootApplication
public class SpringbootWithTestcontainerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootWithTestcontainerApplication.class, args);
	}

	@Bean
	WorkflowClient workflowClient(@Value("${spring.application.temporal.host}") String temporalServerAddress) {
		var workflowServiceFlowService = WorkflowServiceStubsOptions.newBuilder()
				.setTarget(temporalServerAddress) // Temporal server address
				.build();

		return WorkflowClient.newInstance(
				WorkflowServiceStubs.newLocalServiceStubs(),
				/*WorkflowServiceStubs.newServiceStubs(workflowServiceFlowService),*/
				WorkflowClientOptions.newBuilder().build());
	}

	@Bean
	WorkerFactory transferWorkflowWorkerFactory(WorkflowClient workflowClient) {
		var workerFactory = WorkerFactory.newInstance(workflowClient);
		var worker = workerFactory.newWorker("MONEY_TRANSFER_TASK_QUEUE");
		worker.registerWorkflowImplementationTypes(TransferWorkflowImpl.class);
		return workerFactory;
	}

	@EventListener(ApplicationReadyEvent.class)
	void startWorker(WorkerFactory transferWorkflowWorkerFactory) {
		transferWorkflowWorkerFactory.start();
	}

	@Bean
	RouterFunction<ServerResponse> routerFunction(AccountRepository accountRepository, TransferService transferService) {
		return RouterFunctions
				.route()
				.path("/account", builder -> builder
						.GET("", request -> {
							return accountRepository
									.findAll()
									.collectList()
									.flatMap(ServerResponse.ok()::bodyValue);
						})
						.POST("", request -> request.bodyToMono(Account.class).flatMap(accountRepository::save).flatMap(ServerResponse.ok()::bodyValue))
						.GET("/{id}", request -> accountRepository.findById(request.pathVariable("id")).flatMap(ServerResponse.ok()::bodyValue))
						.PUT("/{id}", request -> {
							return accountRepository
									.findById(request.pathVariable("id"))
									.flatMap(dbAccount -> Objects.nonNull(dbAccount) ? request.bodyToMono(Account.class) : Mono.error(new AccountNotFoundException("Account not found")))
									.flatMap(ServerResponse.ok()::bodyValue);
						})
						.PATCH("/{id}", request -> ServerResponse.noContent().build())
						.DELETE("/{id}", request -> accountRepository.deleteById(request.pathVariable("id")).then(ServerResponse.ok().build()))
				)
				.path("/transfer", builder -> builder
						.POST("", request -> transferService.transfer(new Transaction("","",0.0)).flatMap(ServerResponse.ok()::bodyValue))
						.build())
				.build();
	}
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class AccountNotFoundException extends RuntimeException {
	AccountNotFoundException(String id) {
		super("Account not found with id: " + id);
	}
}

@Document
record Account(@MongoId String id, String name, Double amount) {}
interface AccountRepository extends ReactiveMongoRepository<Account, String> {}


// Temporal
@ActivityInterface
interface AccountActivity {
	@ActivityMethod
	void withdraw(String accountId, String referenceId, int amount);

	@ActivityMethod
	void deposit(String accountId, String referenceId, int amount);

	@ActivityMethod
	void refund(String accountId, String referenceId, int amount);
}

class AccountActivityImpl implements AccountActivity {

	@Override
	public void withdraw(String accountId, String referenceId, int amount) {
		System.out.println("Withdrawing " + amount + " from account " + accountId + " with reference " + referenceId);
		// Logic to withdraw amount from the account
	}

	@Override
	public void deposit(String accountId, String referenceId, int amount) {
		System.out.println("Depositing " + amount + " to account " + accountId + " with reference " + referenceId);
		// Logic to deposit amount to the account
	}

	@Override
	public void refund(String accountId, String referenceId, int amount) {
		System.out.println("Refunding " + amount + " to account " + accountId + " with reference " + referenceId);
		// Logic to refund amount to the account
	}
}

@Service
@RequiredArgsConstructor
class TransferService {
	final WorkflowClient workflowClient;

	public Mono<Map<String,Object>> transfer(Transaction transaction) {
		var uuid = UUID.randomUUID().toString();

		TransferWorkflow transferWorkflow = workflowClient
				.newWorkflowStub(
						TransferWorkflow.class,
						WorkflowOptions.newBuilder()
								.setTaskQueue("MONEY_TRANSFER_TASK_QUEUE")
								.setWorkflowId(uuid)
								.build()
				);
		return Mono
				.fromRunnable(() -> WorkflowClient.start(transferWorkflow::transfer, transaction))
				.subscribeOn(Schedulers.boundedElastic())
				.then(
						Mono.just(
								Map.of("uuid", uuid, "status", "Transfer initiated")
						)
				);
	}
}