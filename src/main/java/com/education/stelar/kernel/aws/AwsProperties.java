package com.education.stelar.kernel.aws;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aws")
@Getter
@Setter
public class AwsProperties {

    private String region = "us-east-1";
    private String accessKeyId;
    private String secretAccessKey;
}
