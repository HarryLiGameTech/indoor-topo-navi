package com.e611.toponavi.web.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ApiCorsConfiguration {
    @Bean
    public UrlBasedCorsConfigurationSource apiCorsSource(
            @Value("${platform.api.allow-cors:false}") boolean enabled,
            @Value("${platform.api.cors.allowed-origins:}") String origins,
            @Value("${platform.api.cors.allowed-methods:GET,POST,OPTIONS}") String methods,
            @Value("${platform.api.cors.allowed-headers:Content-Type,Authorization}") String headers,
            @Value("${platform.api.cors.allow-credentials:false}") boolean credentials) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (!enabled) return source;
        List<String> allowedOrigins = split(origins);
        if (allowedOrigins.isEmpty()) throw new IllegalArgumentException("CORS requires explicit allowed origins");
        for (String origin : allowedOrigins) {
            URI uri = URI.create(origin);
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                    || (uri.getPath() != null && !uri.getPath().isEmpty()))
                throw new IllegalArgumentException("CORS origins must be explicit HTTP(S) origins");
        }
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(split(methods));
        configuration.setAllowedHeaders(split(headers));
        configuration.setAllowCredentials(credentials);
        source.registerCorsConfiguration("/api/v1/**", configuration);
        return source;
    }

    private List<String> split(String value) {
        return Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isEmpty()).distinct().toList();
    }
}
