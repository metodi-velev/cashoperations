package com.example.cashoperations.config;

import com.example.cashoperations.interceptor.ApiKeyAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiKeyAuthenticationInterceptor apiKeyAuthenticationInterceptor;

    public WebConfig(ApiKeyAuthenticationInterceptor apiKeyAuthenticationInterceptor) {
        this.apiKeyAuthenticationInterceptor = apiKeyAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyAuthenticationInterceptor);
    }
}