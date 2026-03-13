package com.e611.toponavi.web.auth;

import com.e611.toponavi.web.model.User;
import com.e611.toponavi.web.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Called by Spring Security after the GitHub OAuth flow completes successfully.
 *
 * Redirects to the frontend SPA at {frontendUrl}/auth/callback?token=...
 * so the React AuthCallbackPage can capture and store the JWT.
 * The GitHub OAuth access token is NEVER stored — it is discarded after this handler runs.
 */
@Component
public class GitHubOAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final String frontendUrl;

    public GitHubOAuthSuccessHandler(UserRepository userRepository,
                                     JwtService jwtService,
                                     @Value("${platform.frontend-url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        Long githubId = ((Number) oauthUser.getAttribute("id")).longValue();
        String login = oauthUser.getAttribute("login");
        String name = oauthUser.getAttribute("name");
        String avatarUrl = oauthUser.getAttribute("avatar_url");

        User user = userRepository.findByGithubId(githubId).orElse(new User());
        user.setGithubId(githubId);
        user.setGithubLogin(login);
        user.setDisplayName(name != null ? name : login);
        user.setAvatarUrl(avatarUrl);
        user = userRepository.save(user);

        String jwt = jwtService.issue(user.getId(), login);

        // Absolute redirect to the SPA — works whether frontend is co-hosted or on a separate origin
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/auth/callback?token=" + jwt);
    }
}
