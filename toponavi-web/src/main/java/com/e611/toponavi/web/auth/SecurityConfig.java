package com.e611.toponavi.web.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.e611.toponavi.web.api.ApiException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          GitHubOAuthSuccessHandler oAuthSuccessHandler,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuthSuccessHandler = oAuthSuccessHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiAccessPolicy access,
                                          UrlBasedCorsConfigurationSource apiCorsSource,
                                          ObjectMapper mapper) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(apiCorsSource))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/github", "/auth/callback", "/error", "/oauth2/authorization/github", "/login/oauth2/code/github").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/v1/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/buildings").access((authentication, context) ->
                    new AuthorizationDecision(access.catalogue(authentication.get())))
                .requestMatchers(HttpMethod.POST, "/api/v1/buildings/{buildingId}/**").access((authentication, context) ->
                    new AuthorizationDecision(access.building(authentication.get(), context.getVariables().get("buildingId"))))
                .requestMatchers(HttpMethod.POST, "/api/v1/authoring/validate").access((authentication, context) ->
                    new AuthorizationDecision(access.authoring(authentication.get())))
                .requestMatchers("/api/v1/**").authenticated()
                .requestMatchers("/api/me").authenticated()
                .requestMatchers("/api/maps/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(errors -> errors
                .defaultAuthenticationEntryPointFor((request, response, error) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    mapper.writeValue(response.getOutputStream(), new ApiException(401, "AUTHENTICATION_REQUIRED", "Authentication is required.").body());
                }, new AntPathRequestMatcher("/api/v1/**"))
                .defaultAccessDeniedHandlerFor((request, response, error) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    mapper.writeValue(response.getOutputStream(), new ApiException(403, "ACCESS_DENIED", "Access is denied.").body());
                }, new AntPathRequestMatcher("/api/v1/**")));

        // Only wire up OAuth2 login if credentials are actually configured
        if (clientRegistrationRepository.findByRegistrationId("github") != null) {
            http.oauth2Login(oauth2 -> oauth2
                .clientRegistrationRepository(clientRegistrationRepository)
                .successHandler(oAuthSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    response.setStatus(400);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"OAuth2 login failed\", \"message\": \"" + exception.getMessage() + "\"}");
                })
            );
        }

        return http.build();
    }
}

