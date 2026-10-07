package com.e611.toponavi.web.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiWebConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (request.getQueryString() != null && !request.getQueryString().isEmpty())
                    throw RequestFields.invalid("", "Operation inputs belong in the JSON body, not query parameters.");
                return true;
            }
        }).addPathPatterns("/api/v1/buildings/**", "/api/v1/authoring/**");
    }
}
