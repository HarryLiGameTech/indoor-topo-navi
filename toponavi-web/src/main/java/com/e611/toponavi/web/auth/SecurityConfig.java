package com.e611.toponavi.web.auth;

import com.e611.toponavi.web.api.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Stateless API security with one configured service credential.
 *
 * <ul>
 *   <li>Health and permitted CORS preflights are public; a preflight grants nothing to the actual request.</li>
 *   <li>Project CRUD and source retrieval always require the service credential.</li>
 *   <li>The building catalog, building computation, and request-supplied validation keep their explicit anonymous
 *       allowances from {@link ApiAccessPolicy}.</li>
 *   <li>No sessions, saved requests, login pages, logout endpoints, or redirects; failures answer JSON 401/403.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiAccessPolicy access,
                                           UrlBasedCorsConfigurationSource apiCorsSource, ObjectMapper mapper,
                                           @Value("${platform.security.service-token:}") String serviceToken) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Header credential only; no cookies are used.
            .cors(cors -> cors.configurationSource(apiCorsSource))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(cache -> cache.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/v1/**").permitAll()
                .requestMatchers("/api/v1/projects", "/api/v1/projects/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/buildings").access((authentication, context) ->
                    new AuthorizationDecision(access.catalogue(authentication.get())))
                .requestMatchers(HttpMethod.POST, "/api/v1/buildings/{buildingId}/**").access((authentication, context) ->
                    new AuthorizationDecision(access.building(authentication.get(), context.getVariables().get("buildingId"))))
                .requestMatchers(HttpMethod.POST, "/api/v1/authoring/validate").access((authentication, context) ->
                    new AuthorizationDecision(access.authoring(authentication.get())))
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(new ServiceTokenAuthFilter(serviceToken), AnonymousAuthenticationFilter.class)
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
        return http.build();
    }
}
