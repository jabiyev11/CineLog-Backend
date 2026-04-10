package com.cinelog.service;

import com.cinelog.dto.AuthResponse;
import com.cinelog.dto.LoginRequest;
import com.cinelog.dto.RegisterRequest;
import com.cinelog.entity.Role;
import com.cinelog.entity.User;
import com.cinelog.exception.DuplicateResourceException;
import com.cinelog.exception.UnauthorizedException;
import com.cinelog.repository.UserRepository;
import com.cinelog.security.AppUserDetails;
import com.cinelog.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateResourceException("Username is already in use.");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email is already in use.");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setBio("");
        userRepository.save(user);

        AppUserDetails userDetails = new AppUserDetails(user);
        return new AuthResponse(jwtService.generateToken(userDetails), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        AppUserDetails userDetails = new AppUserDetails(user);
        return new AuthResponse(jwtService.generateToken(userDetails), user.getUsername(), user.getEmail(), user.getRole().name());
    }
}
