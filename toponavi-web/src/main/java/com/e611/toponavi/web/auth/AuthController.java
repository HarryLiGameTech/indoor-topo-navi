package com.e611.toponavi.web.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * Entry point for GitHub OAuth login and token callback.
 */
@RestController
public class AuthController {

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
}

