package com.example.cashoperations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareConfig")
public class CashoperationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashoperationsApplication.class, args);
	}

}
