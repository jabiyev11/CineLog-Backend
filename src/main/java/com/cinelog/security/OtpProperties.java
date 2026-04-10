package com.cinelog.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
        long expirationMinutes,
        long resendCooldownSeconds,
        int maxAttempts
) {
}
