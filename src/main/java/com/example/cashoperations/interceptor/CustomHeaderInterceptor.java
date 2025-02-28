package com.example.cashoperations.interceptor;

import com.example.cashoperations.exception.InvalidApiKeyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CustomHeaderInterceptor implements HandlerInterceptor {

    @Value("${fib.auth.api-key}")
    private String apiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!apiKey.equals(request.getHeader("FIB-X-AUTH"))) {
            throw new InvalidApiKeyException("Invalid API key");
        }
        return true;
    }
}
