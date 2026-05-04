package com.education.stelar.kernel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Email email = new Email();
    private String frontendUrl;

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpiration;
        private long refreshTokenExpiration;
    }

    @Getter
    @Setter
    public static class Email {
        private long verificationTokenExpiration;
        private long resetTokenExpiration;
        private String from;
    }
}
