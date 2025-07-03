package com.example.springboot_with_testcontainer;

import org.springframework.boot.SpringApplication;

public class TestSpringbootWithTestcontainerApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringbootWithTestcontainerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
