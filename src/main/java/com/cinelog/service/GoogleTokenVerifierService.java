package com.cinelog.service;

import com.cinelog.exception.UnauthorizedException;
import com.cinelog.security.GoogleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleTokenVerifierService {

    private final GoogleProperties googleProperties;
    private volatile JwtDecoder jwtDecoder;

    public GoogleUserInfo verify(String idToken) {
        if (!StringUtils.hasText(googleProperties.clientId())) {
            throw new UnauthorizedException("Google sign-in is not configured.");
        }

        final Jwt jwt;
        try {
            jwt = decoder().decode(idToken);
            validateAudience(jwt);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid Google token.");
        }

        String email = jwt.getClaimAsString("email");
        String subject = jwt.getSubject();
        String name = jwt.getClaimAsString("name");
        String picture = jwt.getClaimAsString("picture");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

        if (!Boolean.TRUE.equals(emailVerified) || !StringUtils.hasText(email) || !StringUtils.hasText(subject)) {
            throw new UnauthorizedException("Invalid Google account.");
        }

        return new GoogleUserInfo(subject, email.toLowerCase(), name, picture);
    }

    private JwtDecoder decoder() {
        if (jwtDecoder == null) {
            synchronized (this) {
                if (jwtDecoder == null) {
                    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(googleProperties.jwkSetUri()).build();
                    OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                            JwtValidators.createDefaultWithIssuer(googleProperties.issuer()),
                            this::validateAzpAndAud);
                    decoder.setJwtValidator(validator);
                    jwtDecoder = decoder;
                }
            }
        }
        return jwtDecoder;
    }

    private OAuth2TokenValidatorResult validateAzpAndAud(Jwt jwt) {
        List<String> audience = jwt.getAudience();
        if (audience != null && audience.contains(googleProperties.clientId())) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid Google token audience.", null));
    }

    private void validateAudience(Jwt jwt) {
        List<String> audience = jwt.getAudience();
        if (audience == null || !audience.contains(googleProperties.clientId())) {
            throw new UnauthorizedException("Invalid Google token audience.");
        }
    }

    public record GoogleUserInfo(
            String subject,
            String email,
            String name,
            String pictureUrl
    ) {
    }
}
