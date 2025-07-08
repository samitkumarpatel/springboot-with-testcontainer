package com.example.springboot_with_testcontainer;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	KafkaContainer kafkaContainer() {
		return new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"));
	}

	@Bean
	@ServiceConnection
	MongoDBContainer mongoDbContainer() {
		return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
	}

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
				.withInitScript("schema.sql");
	}


	@Bean
	GenericContainer<?> temporalContainer() {
		return new GenericContainer<>(new ImageFromDockerfile()
				.withDockerfileFromBuilder(builder ->
						builder
								.from("alpine:latest")
								.run("wget 'https://temporal.download/cli/archive/latest?platform=linux&arch=amd64' -O temporal_cli_latest_linux_amd64.tar.gz")
								.run("tar -xf temporal_cli_latest_linux_amd64.tar.gz")
								.run("rm temporal_cli_latest_linux_amd64.tar.gz")
								.run("mv temporal /usr/local/bin")
								.cmd("temporal", "server", "start-dev", "--ip=0.0.0.0", "--log-config")
								.build()))
				.withExposedPorts(8233, 7233)
				//.withAccessToHost(true)
				;
	}

	@Bean
	WorkflowClient workflowClient(@Autowired GenericContainer<?> temporalContainer) {

		if (!temporalContainer.isRunning()) {
			System.out.println("#### Starting Temporal Container, It's not started yet #######");
			temporalContainer.start();
		}
		// Execute your command inside the container

		Map.of(
				"FirstName", "Keyword",
				"LastName", "Keyword",
				"AttributeThree", "Keyword",
				"AttributeFour", "Keyword"
		).forEach((k,v) -> {
			try{
				temporalContainer.execInContainer(
						"temporal", "operator", "search-attribute", "create", "--name", k, "--type", v
				);
			} catch (Exception e) {
				System.err.println("Failed to create search attribute: " + k + " of type " + v);
				e.getMessage();
			}
			finally {
				System.out.println("Search attribute created: " + k + " of type " + v);
			}
		});
//		temporalContainer.execInContainer(
//				"temporal", "operator", "search-attribute", "create", "--name", "YourAttributeName", "--type", "Keyword"
//		);

		var workflowServiceFlowService = WorkflowServiceStubsOptions.newBuilder()
				.setTarget(temporalContainer.getHost()+":"+temporalContainer.getMappedPort(7233)) // Temporal server address
				.build();

		return WorkflowClient.newInstance(WorkflowServiceStubs.newServiceStubs(workflowServiceFlowService), WorkflowClientOptions.newBuilder().build());
	}

}
