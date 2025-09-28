package com.example.cashdocumentsservice;

import com.example.cashdocumentsservice.dto.CashDocumentsServiceContactInfoDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableConfigurationProperties(value = {CashDocumentsServiceContactInfoDto.class})
@EnableJpaAuditing(auditorAwareRef = "auditAwareConfig")
public class CashDocumentsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashDocumentsServiceApplication.class, args);
	}

}
