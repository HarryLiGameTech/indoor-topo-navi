package com.e611.toponavi.web.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ApiAccessPolicy {
    private final List<String> anonymousBuildings;
    private final boolean guestAuthoring;

    public ApiAccessPolicy(@Value("${platform.api.anonymous-buildings:}") String anonymousBuildings,
                           @Value("${platform.api.authoring.allow-guests:false}") boolean guestAuthoring) {
        this.anonymousBuildings = Arrays.stream(anonymousBuildings.split(",")).map(String::trim)
                .filter(value -> !value.isEmpty()).distinct().toList();
        this.guestAuthoring = guestAuthoring;
    }

    public boolean authenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public boolean catalogue(Authentication authentication) {
        return authenticated(authentication) || !anonymousBuildings.isEmpty();
    }

    public boolean building(Authentication authentication, String buildingId) {
        return authenticated(authentication) || anonymousBuildings.contains(buildingId);
    }

    public boolean authoring(Authentication authentication) {
        return authenticated(authentication) || guestAuthoring;
    }

    public List<String> visibleBuildings(Authentication authentication, List<String> buildings) {
        return authenticated(authentication) ? buildings : buildings.stream().filter(anonymousBuildings::contains).toList();
    }
}
