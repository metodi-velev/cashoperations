package com.example.cashreportingservice;

import com.example.cashreportingservice.dto.CashreportingserviceContactInfoDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableConfigurationProperties(value = {CashreportingserviceContactInfoDto.class})
@EnableJpaAuditing(auditorAwareRef = "auditAwareConfig")
public class CashReportingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashReportingServiceApplication.class, args);
	}

}
