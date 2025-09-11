package com.example.cashoperations;

import com.example.cashoperations.dto.CashoperationsContactInfoDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableConfigurationProperties(value = {CashoperationsContactInfoDto.class})
@EnableJpaAuditing(auditorAwareRef = "auditAwareConfig")
public class CashoperationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashoperationsApplication.class, args);
	}

}
