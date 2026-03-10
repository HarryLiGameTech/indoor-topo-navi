package com.e611.toponavi.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class AuthController implements ErrorController {

    @GetMapping("/auth/github")
    public void initiateGitHubLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/github");
    }

    /**
     * After successful OAuth, GitHubOAuthSuccessHandler redirects here with the JWT.
     * Returns the token as JSON so it can be easily copied for use in Postman.
     */
    @GetMapping("/auth/callback")
    public ResponseEntity<Map<String, String>> callback(@RequestParam String token) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("token", token));
    }

    /**
     * Replaces Spring's Whitelabel error page with a JSON error response.
     * Makes it easier to diagnose what's going wrong during the OAuth flow.
     */
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

