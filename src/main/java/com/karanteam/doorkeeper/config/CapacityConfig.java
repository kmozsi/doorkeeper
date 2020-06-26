package com.karanteam.doorkeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
@Data
public class CapacityConfig {
    private Integer initialCapacity;
    private Integer initialPercentage;
    private Integer initialMinDistance;
}
