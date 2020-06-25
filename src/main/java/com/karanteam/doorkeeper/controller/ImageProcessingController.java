package com.karanteam.doorkeeper.controller;

import com.karanteam.doorkeeper.data.PositionConfiguration;
import com.karanteam.doorkeeper.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class ImageProcessingController {

    private final ImageService imageService;

    public ImageProcessingController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(
        value = "/setPositions",
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> setPositions(
        @RequestParam(required = false, defaultValue = "false") boolean filtered,
        @RequestParam(required = false, defaultValue = "false") boolean gray,
        @RequestParam(required = false, defaultValue = "0")  int threshold,
        @RequestParam(required = false, defaultValue = "0")  int maxValue,
        @RequestParam(required = false, defaultValue = "3")  int threshMethod,
        @RequestBody PositionConfiguration configuration
        ) throws IOException {
        return ResponseEntity.ok(imageService.processImage(gray, filtered, configuration, threshold, maxValue, threshMethod));
    }

    @GetMapping(
        value = "/findPositions",
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> findPositions(
        @RequestParam(required = false, defaultValue = "false") boolean filtered,
        @RequestParam(required = false, defaultValue = "false") boolean gray,
        @RequestParam(required = false, defaultValue = "0")  int threshold,
        @RequestParam(required = false, defaultValue = "0")  int maxValue,
        @RequestParam(required = false, defaultValue = "3")  int threshMethod,
        @RequestParam(required = false, defaultValue = "3")  int matchingThreshold
    ) throws IOException {
        return ResponseEntity.ok(imageService.findPositions(gray, filtered, threshold, maxValue, threshMethod, matchingThreshold));
    }

}
