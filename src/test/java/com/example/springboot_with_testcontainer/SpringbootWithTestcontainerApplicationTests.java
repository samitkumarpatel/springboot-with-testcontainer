package com.example.springboot_with_testcontainer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SpringbootWithTestcontainerApplicationTests {

	@Test
	void contextLoads() {
	}

}
