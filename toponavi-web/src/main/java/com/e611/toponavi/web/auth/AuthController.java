package com.e611.toponavi.web.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Entry point for GitHub OAuth login.
 * GET /auth/github redirects the browser to Spring Security's built-in
 * OAuth2 authorization endpoint, which then redirects to GitHub.
 */
@RestController
public class AuthController {

    @GetMapping("/auth/github")
    public void initiateGitHubLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/github");
    }
}

