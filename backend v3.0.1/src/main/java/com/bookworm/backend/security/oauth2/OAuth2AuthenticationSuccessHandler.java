package com.bookworm.backend.security.oauth2;

import com.bookworm.backend.entity.User;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.security.JwtService;
import com.bookworm.backend.security.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Fires once Spring Security has finished the Google OAuth2/OIDC handshake and
 * CustomOidcUserService has found-or-created the matching User row. Mints the
 * exact same JWT AuthServiceImpl.login()/register() issue (via JwtService +
 * UserPrincipal), then redirects to the SPA with the token as a query param -
 * this is a browser redirect flow, not a JSON API response, so there's no
 * other way to hand the token back to the frontend.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${bookworm.frontend.oauth2-callback-url:http://localhost:5173/oauth2/callback}")
    private String frontendCallbackUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();

        User user = userRepository.findWithRoleByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Google-authenticated user vanished: " + email));

        String token = jwtService.generateToken(new UserPrincipal(user));

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                .queryParam("token", token)
                .queryParam("userId", user.getUserId())
                .queryParam("email", user.getEmail())
                .queryParam("fullName", user.getFullName())
                .queryParam("role", user.getRole().getRoleName())
                .build()
                .toUriString();

        log.info("Google sign-in succeeded for {}, redirecting to frontend callback", email);
        response.sendRedirect(redirectUrl);
    }
}
