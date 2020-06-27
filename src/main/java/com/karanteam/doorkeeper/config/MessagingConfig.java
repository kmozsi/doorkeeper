package com.karanteam.doorkeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "messaging")
@Data
public class MessagingConfig {
    private String topic;
    private String serverLocation;
    private String clientId;
    private Integer notifyPosition;
}
