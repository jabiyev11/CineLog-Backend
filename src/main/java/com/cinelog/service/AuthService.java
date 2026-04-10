package com.cinelog.service;

import com.cinelog.dto.AuthResponse;
import com.cinelog.dto.GoogleAuthRequest;
import com.cinelog.dto.LoginRequest;
import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.OtpVerificationRequest;
import com.cinelog.dto.RegisterRequest;
import com.cinelog.dto.ResendOtpRequest;
import com.cinelog.entity.AuthProvider;
import com.cinelog.entity.EmailVerificationOtp;
import com.cinelog.entity.Role;
import com.cinelog.entity.User;
import com.cinelog.exception.BadRequestException;
import com.cinelog.exception.DuplicateResourceException;
import com.cinelog.exception.UnauthorizedException;
import com.cinelog.repository.EmailVerificationOtpRepository;
import com.cinelog.repository.UserRepository;
import com.cinelog.security.AppUserDetails;
import com.cinelog.security.JwtService;
import com.cinelog.security.OtpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpProperties otpProperties;
    private final EmailService emailService;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        validateUniqueRegistration(request.username(), request.email());

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setBio("");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);
        userRepository.save(user);

        sendFreshOtp(user, true);
        return new MessageResponse("Verification OTP sent to your email.");
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (user.getAuthProvider() == AuthProvider.GOOGLE && user.getPasswordHash() == null) {
            throw new UnauthorizedException("Use Google sign-in for this account.");
        }
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email is not verified.");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse googleSignIn(GoogleAuthRequest request) {
        GoogleTokenVerifierService.GoogleUserInfo googleUser = googleTokenVerifierService.verify(request.idToken());

        User user = userRepository.findByGoogleSubject(googleUser.subject())
                .or(() -> userRepository.findByEmailIgnoreCase(googleUser.email()))
                .orElseGet(() -> createGoogleUser(googleUser));

        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setGoogleSubject(googleUser.subject());
        user.setEmailVerified(true);
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        if (googleUser.pictureUrl() != null && !googleUser.pictureUrl().isBlank()) {
            user.setProfilePictureUrl(googleUser.pictureUrl());
        }
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public MessageResponse verifyOtp(OtpVerificationRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid verification request."));

        EmailVerificationOtp otp = otpRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BadRequestException("OTP not found. Please request a new one."));

        if (otp.isUsed()) {
            throw new BadRequestException("OTP has already been used.");
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }
        if (otp.getAttemptCount() >= otpProperties.maxAttempts()) {
            throw new BadRequestException("OTP attempts exceeded. Please request a new one.");
        }

        otp.setAttemptCount(otp.getAttemptCount() + 1);
        if (!passwordEncoder.matches(request.otp(), otp.getCodeHash())) {
            otpRepository.save(otp);
            throw new BadRequestException("Invalid OTP.");
        }

        otp.setUsed(true);
        otpRepository.save(otp);

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        return new MessageResponse("Email verified successfully.");
    }

    @Transactional
    public MessageResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid resend request."));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified.");
        }
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("OTP verification is not available for this account.");
        }

        sendFreshOtp(user, false);
        return new MessageResponse("A new OTP has been sent to your email.");
    }

    private void validateUniqueRegistration(String username, String email) {
        if (userRepository.existsByUsernameIgnoreCase(username.trim())) {
            throw new DuplicateResourceException("Username is already in use.");
        }
        if (userRepository.existsByEmailIgnoreCase(email.trim().toLowerCase())) {
            throw new DuplicateResourceException("Email is already in use.");
        }
    }

    private void sendFreshOtp(User user, boolean firstSend) {
        EmailVerificationOtp latestOtp = otpRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId()).orElse(null);
        if (!firstSend && latestOtp != null) {
            LocalDateTime allowedResendAt = latestOtp.getCreatedAt().plusSeconds(otpProperties.resendCooldownSeconds());
            if (allowedResendAt.isAfter(LocalDateTime.now())) {
                throw new BadRequestException("Please wait before requesting another OTP.");
            }
        }

        String rawOtp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        EmailVerificationOtp otp = new EmailVerificationOtp();
        otp.setUser(user);
        otp.setCodeHash(passwordEncoder.encode(rawOtp));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(otpProperties.expirationMinutes()));
        otp.setUsed(false);
        otp.setAttemptCount(0);
        otpRepository.save(otp);
        emailService.sendOtpEmail(user.getEmail(), rawOtp);
    }

    private AuthResponse buildAuthResponse(User user) {
        AppUserDetails userDetails = new AppUserDetails(user);
        return new AuthResponse(jwtService.generateToken(userDetails), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    private User createGoogleUser(GoogleTokenVerifierService.GoogleUserInfo googleUser) {
        User user = new User();
        user.setUsername(generateUniqueUsername(googleUser));
        user.setEmail(googleUser.email());
        user.setPasswordHash(null);
        user.setRole(Role.USER);
        user.setBio("");
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setGoogleSubject(googleUser.subject());
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setProfilePictureUrl(googleUser.pictureUrl());
        return userRepository.save(user);
    }

    private String generateUniqueUsername(GoogleTokenVerifierService.GoogleUserInfo googleUser) {
        String source = googleUser.name() != null && !googleUser.name().isBlank()
                ? googleUser.name()
                : googleUser.email().substring(0, googleUser.email().indexOf('@'));
        String base = source.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.length() < 3) {
            base = "user" + base;
        }
        if (base.length() > 24) {
            base = base.substring(0, 24);
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            String suffixText = String.valueOf(suffix++);
            int maxBaseLength = Math.max(1, 24 - suffixText.length());
            candidate = base.substring(0, Math.min(base.length(), maxBaseLength)) + suffixText;
        }
        return candidate;
    }
}
