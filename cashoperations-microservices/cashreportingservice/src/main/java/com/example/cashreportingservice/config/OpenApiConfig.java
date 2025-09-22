package com.example.cashreportingservice.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cashoperations microservice REST API Documentation",
                description = "Deposit and Withdrawal in BGN and EUR Cashoperations microservice REST API Documentation",
                version = "v1",
                contact = @Contact(
                        name = "Metodi Velev",
                        email = "metodi.velev@example.com",
                        url = "https://www.example.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.example.com"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description =  "Deposit and Withdrawal in BGN and EUR Cashoperations microservice REST API Documentation",
                url = "https://www.example.com/swagger-ui.html"
        )
)
@SecurityScheme(
        name = "fibAuth",                    // reference name
        type = SecuritySchemeType.APIKEY,    // API key style
        in = SecuritySchemeIn.HEADER,        // put it in the header
        paramName = "FIB-X-AUTH"             // 👈 your header name
)
public class OpenApiConfig {
}
