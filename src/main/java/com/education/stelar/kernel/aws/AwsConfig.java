package com.education.stelar.kernel.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

/**
 * Produce el AwsCredentialsProvider compartido para todos los servicios AWS.
 * Si aws.access-key-id está definido, usa credenciales estáticas (dev/staging con clave explícita).
 * Si no, usa DefaultCredentialsProvider (IAM role, env vars, ~/.aws/credentials).
 */
@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class AwsConfig {

    private final AwsProperties awsProperties;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (awsProperties.getAccessKeyId() != null && !awsProperties.getAccessKeyId().isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            awsProperties.getAccessKeyId(),
                            awsProperties.getSecretAccessKey()));
        }
        return DefaultCredentialsProvider.create();
    }
}
