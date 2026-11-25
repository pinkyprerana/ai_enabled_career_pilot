package com.example.learningSpringBootApp.security.oauth2;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        processOAuth2User(registrationId, oAuth2User);
        return oAuth2User;
    }

    public UserEntity processOAuth2User(String provider, OAuth2User oAuth2User) {
        String email = extractEmail(provider, oAuth2User);
        String name = extractName(provider, oAuth2User);

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setName(name);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity(
                            name,
                            email,
                            passwordEncoder.encode(UUID.randomUUID().toString()),
                            "ROLE_USER",
                            provider.toUpperCase()
                    );
                    return userRepository.save(newUser);
                });
    }

    public String extractEmail(String provider, OAuth2User user) {
        Object emailObj = user.getAttribute("email");
        if (emailObj != null && !emailObj.toString().isBlank()) {
            return emailObj.toString().trim().toLowerCase();
        }

        // Check nested structure (e.g., Twitter v2 API returns under "data")
        Object dataObj = user.getAttribute("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Object email = dataMap.get("email");
            if (email != null && !email.toString().isBlank()) {
                return email.toString().trim().toLowerCase();
            }
            Object username = dataMap.get("username");
            if (username != null && !username.toString().isBlank()) {
                return username.toString().trim().toLowerCase() + "@twitter.user";
            }
        }

        // Fallbacks for providers that don't return public email
        if ("github".equalsIgnoreCase(provider)) {
            Object login = user.getAttribute("login");
            if (login != null && !login.toString().isBlank()) {
                return login.toString().trim().toLowerCase() + "@github.user";
            }
        } else if ("twitter".equalsIgnoreCase(provider)) {
            Object username = user.getAttribute("username");
            if (username != null && !username.toString().isBlank()) {
                return username.toString().trim().toLowerCase() + "@twitter.user";
            }
        }

        return "user_" + user.getName().toLowerCase() + "@" + provider.toLowerCase() + ".user";
    }

    public String extractName(String provider, OAuth2User user) {
        Object nameObj = user.getAttribute("name");
        if (nameObj != null && !nameObj.toString().isBlank()) {
            return nameObj.toString();
        }

        Object dataObj = user.getAttribute("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Object name = dataMap.get("name");
            if (name != null && !name.toString().isBlank()) {
                return name.toString();
            }
            Object username = dataMap.get("username");
            if (username != null && !username.toString().isBlank()) {
                return username.toString();
            }
        }

        if ("github".equalsIgnoreCase(provider)) {
            Object login = user.getAttribute("login");
            if (login != null) return login.toString();
        } else if ("twitter".equalsIgnoreCase(provider)) {
            Object username = user.getAttribute("username");
            if (username != null) return username.toString();
        }

        return provider.substring(0, 1).toUpperCase() + provider.substring(1) + " User";
    }
}
