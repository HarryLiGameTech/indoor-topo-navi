package com.e611.toponavi.web.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Recognizes the one configured opaque service credential sent as {@code Authorization: Bearer <token>} and
 * establishes an authenticated service principal. There is no account lookup, database access, token issuance, or
 * JWT parsing. An absent or wrong credential establishes nothing, so protected routes answer 401 while explicitly
 * public routes continue as anonymous.
 *
 * <p>Not a Spring bean: it is registered once, inside the security filter chain, and never as a servlet filter.
 */
public class ServiceTokenAuthFilter extends OncePerRequestFilter {
    public static final String SERVICE_PRINCIPAL = "toponavi-service";
    static final int MIN_TOKEN_LENGTH = 32;
    /** RFC 6750 b64token characters, so the credential is always transmissible as a Bearer value. */
    private static final Pattern TOKEN68 = Pattern.compile("[A-Za-z0-9._~+/-]+=*");
    private static final String BEARER = "Bearer ";

    private final byte[] expectedDigest;

    public ServiceTokenAuthFilter(String configuredToken) {
        if (configuredToken == null || configuredToken.isBlank())
            throw new IllegalStateException("platform.security.service-token (TOPONAVI_SERVICE_TOKEN) must be configured.");
        if (configuredToken.length() < MIN_TOKEN_LENGTH)
            throw new IllegalStateException("platform.security.service-token must be at least " + MIN_TOKEN_LENGTH + " characters.");
        if (!TOKEN68.matcher(configuredToken).matches())
            throw new IllegalStateException("platform.security.service-token must contain only letters, digits, '-', '.', '_', '~', '+', '/' and trailing '='.");
        this.expectedDigest = digest(configuredToken);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, BEARER, 0, BEARER.length())
                // Fixed-length digests compared in constant time, so timing does not reveal the token.
                && MessageDigest.isEqual(digest(header.substring(BEARER.length())), expectedDigest)) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    SERVICE_PRINCIPAL, null, List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))));
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
