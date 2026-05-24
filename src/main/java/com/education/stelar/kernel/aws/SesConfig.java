package com.education.stelar.kernel.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class SesConfig {

    private final AwsProperties awsProperties;
    private final AwsCredentialsProvider awsCredentialsProvider;

    @Bean
    public SesV2Client sesV2Client() {
        return SesV2Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}
