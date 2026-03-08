package com.e611.toponavi.web.auth;

import com.e611.toponavi.web.model.User;
import com.e611.toponavi.web.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Called by Spring Security after the GitHub OAuth flow completes successfully.
 *
 * Responsibilities:
 *  1. Extract GitHub user identity from the OAuth2User principal
 *  2. Upsert the user record in the platform database
 *  3. Issue a platform JWT
 *  4. Redirect to the frontend with the JWT in a query parameter
 *     (the frontend reads it once, stores it, and discards it from the URL)
 *
 * The GitHub OAuth access token is NEVER stored — it is discarded after this handler runs.
 */
@Component
public class GitHubOAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public GitHubOAuthSuccessHandler(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        // Extract identity from GitHub OAuth user info
        Long githubId = ((Number) oauthUser.getAttribute("id")).longValue();
        String login = oauthUser.getAttribute("login");
        String name = oauthUser.getAttribute("name");
        String avatarUrl = oauthUser.getAttribute("avatar_url");

        // Upsert user in platform DB
        User user = userRepository.findByGithubId(githubId).orElse(new User());
        user.setGithubId(githubId);
        user.setGithubLogin(login);
        user.setDisplayName(name != null ? name : login);
        user.setAvatarUrl(avatarUrl);
        user = userRepository.save(user);

        // Issue platform JWT — GitHub OAuth token is discarded here
        String jwt = jwtService.issue(user.getId(), login);

        // Redirect to frontend with token
        getRedirectStrategy().sendRedirect(request, response, "/auth/callback?token=" + jwt);
    }
}

