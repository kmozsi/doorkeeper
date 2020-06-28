package com.karanteam.doorkeeper.config;

import com.karanteam.doorkeeper.data.ImageProcessingConfiguration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
@Data
public class ApplicationConfig {
    private Integer initialCapacity;
    private Integer initialPercentage;
    private Integer initialMinDistance;
    private Integer pixelSizeInCm;
    private ImageProcessingConfiguration image;
}
