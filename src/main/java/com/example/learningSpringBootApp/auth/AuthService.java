package com.example.learningSpringBootApp.auth;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.dto.AuthResponse;
import com.example.learningSpringBootApp.dto.LoginRequest;
import com.example.learningSpringBootApp.dto.SignupRequest;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.exception.DuplicateResourceException;
import com.example.learningSpringBootApp.repository.UserRepository;
import com.example.learningSpringBootApp.security.JwtUtils;
import com.example.learningSpringBootApp.security.TokenBlacklistService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       AuthenticationManager authenticationManager,
                       TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email '" + request.email() + "' is already registered");
        }

        String role = "ROLE_USER";
        UserEntity user = new UserEntity(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                role
        );
        UserEntity savedUser = userRepository.save(user);

        String token = jwtUtils.generateToken(savedUser.getEmail(), savedUser.getRole());
        return new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Date expiration = jwtUtils.extractExpiration(token);
                tokenBlacklistService.blacklistToken(token, expiration);
            } catch (Exception ignored) {
            }
        }
        SecurityContextHolder.clearContext();
    }
}
