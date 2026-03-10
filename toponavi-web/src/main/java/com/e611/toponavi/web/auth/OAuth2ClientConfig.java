package com.e611.toponavi.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Manually registers OAuth2 client beans.
 * Replaces OAuth2ClientAutoConfiguration which we excluded to avoid
 * startup failure when credentials are not set.
 */
@Configuration
public class OAuth2ClientConfig {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientConfig.class);

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret:}")
    private String clientSecret;

    @Value("${platform.github.oauth-base-url:}")
    private String oauthBaseUrl;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        log.info("OAuth2ClientConfig: clientId='{}' blank={}", clientId, clientId == null || clientId.isBlank());
        if (clientId == null || clientId.isBlank()) {
            return registrationId -> null;
        }

        // Use explicit base URL if configured (needed when behind a proxy that doesn't
        // forward X-Forwarded-Proto, causing Spring to build http:// redirect URIs instead of https://)
        String redirectUri = (oauthBaseUrl != null && !oauthBaseUrl.isBlank())
                ? oauthBaseUrl + "/login/oauth2/code/github"
                : "{baseUrl}/login/oauth2/code/{registrationId}";

        log.info("OAuth2ClientConfig: using redirectUri='{}'", redirectUri);

        ClientRegistration githubRegistration = ClientRegistration
                .withRegistrationId("github")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scope("read:user", "user:email")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("login")
                .clientName("GitHub")
                .build();

        return new InMemoryClientRegistrationRepository(githubRegistration);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }
}

