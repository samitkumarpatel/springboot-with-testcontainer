package com.example.springboot_with_testcontainer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

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


//	@Bean
//	GenericContainer<?> temporalContainer() {
//		return new GenericContainer<>(new ImageFromDockerfile()
//				.withDockerfileFromBuilder(builder ->
//						builder
//								.from("alpine:latest")
//								.run("wget 'https://temporal.download/cli/archive/latest?platform=linux&arch=amd64' -O temporal_cli_latest_linux_amd64.tar.gz")
//								.run("tar -xf temporal_cli_latest_linux_amd64.tar.gz")
//								.run("rm temporal_cli_latest_linux_amd64.tar.gz")
//								.run("mv temporal /usr/local/bin")
//								.cmd("temporal", "server", "start-dev", "--ui-ip=0.0.0.0", "--log-config")
//								.build()))
//				.withExposedPorts(8233, 7233)
//				.withAccessToHost(true);
//	}

}
