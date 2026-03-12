package com.e611.toponavi.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class AuthController implements ErrorController {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public AuthController(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @GetMapping("/auth/github")
    public void initiateGitHubLogin(HttpServletResponse response) throws IOException {
        ClientRegistration reg = clientRegistrationRepository.findByRegistrationId("github");
        if (reg == null) {
            response.sendError(503, "GitHub OAuth is not configured");
            return;
        }
        // Redirect directly to Spring Security's OAuth2 authorization endpoint
        // which is registered by the oauth2Login() filter chain
        response.sendRedirect("/oauth2/authorization/github");
    }


    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Object message = request.getAttribute("jakarta.servlet.error.message");
        Object exception = request.getAttribute("jakarta.servlet.error.exception");
        return ResponseEntity.status(status instanceof Integer i ? i : 500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "status", status != null ? status : 500,
                        "message", message != null ? message : "",
                        "exception", exception != null ? exception.toString() : ""
                ));
    }
}




