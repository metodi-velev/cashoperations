package com.example.cashoperations.interceptor;

import com.example.cashoperations.exception.InvalidApiKeyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyAuthenticationInterceptor implements HandlerInterceptor {

    private final String apiKey;

    public ApiKeyAuthenticationInterceptor(@Value("${fib.auth.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        // ✅ allow Swagger + API docs without API key
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return true;
        }
        if (path.startsWith("/h2-console")) {
            return true;
        }
        if (path.startsWith("/stats")) {
            return true;
        }
        if (!apiKey.equals(request.getHeader("FIB-X-AUTH"))) {
            throw new InvalidApiKeyException("Invalid API key.");
        }
        return true;
    }
}
