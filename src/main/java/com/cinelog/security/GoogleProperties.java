package com.cinelog.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public record GoogleProperties(
        String clientId,
        String jwkSetUri,
        String issuer
) {
}
