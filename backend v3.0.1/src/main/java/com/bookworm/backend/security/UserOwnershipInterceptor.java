package com.bookworm.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/**
 * Every controller nested under /api/v1/users/{userId}/... (Cart, Purchase,
 * Rent, User Library) still takes userId as a plain @PathVariable rather
 * than reading it off the token - this interceptor is what actually makes
 * that safe: it rejects any request where the path's userId doesn't match
 * the authenticated principal's userId, so a valid token for user 5 can
 * never touch /users/6/cart. ADMIN bypasses the check (support/back-office
 * access to any user's data). Registered only against the /users/{userId}/**
 * pattern in WebConfig - it never runs for routes without that segment.
 */
@Component
public class UserOwnershipInterceptor implements HandlerInterceptor {

    @SuppressWarnings("unchecked")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables == null || !pathVariables.containsKey("userId")) {
            // No {userId} segment on this route - nothing for this interceptor to enforce.
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return true;
        }

        Long pathUserId;
        try {
            pathUserId = Long.valueOf(pathVariables.get("userId"));
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid userId");
        }

        if (!principal.getUserId().equals(pathUserId)) {
            throw new AccessDeniedException("You may only access your own resources");
        }

        return true;
    }
}
