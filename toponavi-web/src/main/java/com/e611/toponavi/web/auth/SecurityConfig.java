package com.e611.toponavi.web.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * Security model:
 *  - OAuth2 login is used ONLY as the identity provider; the resulting OAuth token
 *    is immediately discarded — we issue our own platform JWT instead.
 *  - All /api/** endpoints require a valid platform JWT.
 *  - /auth/github is the entry point that triggers the OAuth flow.
 *  - /login/oauth2/code/github is the OAuth callback handled by Spring's built-in filter.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final GitHubOAuthSuccessHandler oAuthSuccessHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          GitHubOAuthSuccessHandler oAuthSuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuthSuccessHandler = oAuthSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // OAuth endpoints are public
                .requestMatchers("/auth/github", "/login/oauth2/code/github").permitAll()
                // Existing topology / DSL endpoints remain public
                .requestMatchers("/api/v1/**").permitAll()
                // New map management API requires authentication
                .requestMatchers("/api/maps/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuthSuccessHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

