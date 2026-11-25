package com.example.learningSpringBootApp.security.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.security.JwtUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final CustomOAuth2UserService customOAuth2UserService;

    public OAuth2AuthenticationSuccessHandler(JwtUtils jwtUtils,
                                              CustomOAuth2UserService customOAuth2UserService) {
        this.jwtUtils = jwtUtils;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            UserEntity user = customOAuth2UserService.processOAuth2User(registrationId, oAuth2User);
            String token = jwtUtils.generateToken(user.getEmail(), user.getRole());

            String targetUrl = "/?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                    + "&name=" + URLEncoder.encode(user.getName(), StandardCharsets.UTF_8)
                    + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                    + "&role=" + URLEncoder.encode(user.getRole(), StandardCharsets.UTF_8)
                    + "&provider=" + URLEncoder.encode(registrationId, StandardCharsets.UTF_8);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
