package com.example.livetask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.livetask")
@EntityScan(basePackages = "com.example.livetask.model")
@EnableJpaRepositories(basePackages = "com.example.livetask.repository")

public class LivetaskApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(LivetaskApplication.class, args);
	}
}
