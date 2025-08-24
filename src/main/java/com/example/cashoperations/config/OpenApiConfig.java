package com.example.cashoperations.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
@SecurityScheme(
        name = "fibAuth",                    // reference name
        type = SecuritySchemeType.APIKEY,    // API key style
        in = SecuritySchemeIn.HEADER,        // put it in the header
        paramName = "FIB-X-AUTH"             // 👈 your header name
)
public class OpenApiConfig {
}
