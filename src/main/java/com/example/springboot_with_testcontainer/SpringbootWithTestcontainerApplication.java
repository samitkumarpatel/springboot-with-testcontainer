package com.example.springboot_with_testcontainer;

import com.example.springboot_with_testcontainer.model.Account;
import com.example.springboot_with_testcontainer.model.Customer;
import com.example.springboot_with_testcontainer.model.Transaction;
import com.example.springboot_with_testcontainer.repository.AccountRepository;
import com.example.springboot_with_testcontainer.temporal.*;
import com.example.springboot_with_testcontainer.utility.AccountNotFoundException;
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
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
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
	ApplicationListener<ApplicationReadyEvent> onApplicationReady(WorkflowClient workflowClient, TransferActivityImpl transferActivity) {
		return event -> {
			var workerFactory = WorkerFactory.newInstance(workflowClient);
			//money transferWorker
			var moneyTransferWorker = workerFactory.newWorker("MONEY_TRANSFER_TASK_QUEUE");
			moneyTransferWorker.registerWorkflowImplementationTypes(TransferWorkflowImpl.class);
			moneyTransferWorker.registerActivitiesImplementations(transferActivity);

			//user creation worker
			var userCreationWorkflow = workerFactory.newWorker("USER_CREATION_TASK_QUEUE");
			userCreationWorkflow.registerWorkflowImplementationTypes(UserCreationWorkflowImpl.class);
			userCreationWorkflow.registerActivitiesImplementations(new UserCreationActivityImpl());

			workerFactory.start();
		};
	}

	@Bean
	RouterFunction<ServerResponse> routerFunction(
			AccountRepository accountRepository,
			TransferService transferService,
			UserCreationService userCreationService) {
		return RouterFunctions
				.route()
				.path("/account", builder -> builder
						.GET("", request -> {
							return Mono.fromCallable(accountRepository::findAll)
									.flatMap(ServerResponse.ok()::bodyValue);
						})
						.POST("", request -> request.bodyToMono(Account.class)
								.map(accountRepository::save)
								.flatMap(ServerResponse.ok()::bodyValue))
						.GET("/{id}", request -> Mono.fromCallable(() -> accountRepository.findById(Long.valueOf(request.pathVariable("id"))))
								.flatMap(ServerResponse.ok()::bodyValue))
						.PUT("/{id}", request -> Mono.fromCallable(() -> accountRepository
									.findById(Long.valueOf(request.pathVariable("id"))))
									.flatMap(dbAccount -> Objects.nonNull(dbAccount) ? request.bodyToMono(Account.class) : Mono.error(new AccountNotFoundException("Account not found")))
									.flatMap(ServerResponse.ok()::bodyValue)
						)
						.PATCH("/{id}", request -> ServerResponse.noContent().build())
						.DELETE("/{id}", request -> Mono.fromRunnable(() -> accountRepository.deleteById(Long.valueOf(request.pathVariable("id"))))
								.then(ServerResponse.ok().build()))
				)
				.path("/transfer", builder -> builder
						.POST("", request ->
								request
										.bodyToMono(Transaction.class)
												.flatMap(transferService::transfer)
												.flatMap(ServerResponse.ok()::bodyValue))
				)
				.path("/user", builder -> builder
						.POST("", request ->
								request
										.bodyToMono(Customer.class)
												.flatMap(userCreationService::createUser)
												.flatMap(ServerResponse.ok()::bodyValue))
				)
				.build();
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

@Service
@RequiredArgsConstructor
class UserCreationService {
	final WorkflowClient workflowClient;

	public Mono<Map<String,Object>> createUser(Customer customer) {
		var uuid = UUID.randomUUID().toString();

		UserCreationWorkflow userCreationWorkflow = workflowClient
				.newWorkflowStub(
						UserCreationWorkflow.class,
						WorkflowOptions.newBuilder()
								.setTaskQueue("USER_CREATION_TASK_QUEUE")
								.setWorkflowId(uuid)
								.build()
				);
		return Mono
				.fromRunnable(() -> WorkflowClient.start(userCreationWorkflow::newUserFlow, customer))
				.subscribeOn(Schedulers.boundedElastic())
				.then(
						Mono.just(
								Map.of("uuid", uuid, "status", "User creation initiated")
						)
				);
	}
}