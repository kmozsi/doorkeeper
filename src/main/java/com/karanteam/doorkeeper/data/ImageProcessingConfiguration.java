package com.karanteam.doorkeeper.data;

import lombok.Data;

@Data
public class ImageProcessingConfiguration {
    private Boolean grayScale;
    private Integer threshold;
    private Integer maxValue;
    private Integer thresholdMethod;
    private Integer matchingThreshold;

    private Integer coloringThreshold;
    private Integer coloringThresholdMethod;
    private Integer coloringMaxValue;
}
