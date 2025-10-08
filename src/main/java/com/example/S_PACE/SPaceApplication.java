package com.example.S_PACE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.S_PACE.repository")
@EnableTransactionManagement
public class SPaceApplication {
	public static void main(String[] args) {
		SpringApplication.run(SPaceApplication.class, args);
	}
}