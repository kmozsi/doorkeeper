package com.karanteam.doorkeeper.controller;

import com.karanteam.doorkeeper.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ImageProcessingController {

    private final ImageService imageService;

    public ImageProcessingController(ImageService imageService) {
        this.imageService = imageService;
    }

//        public static final int THRESH_BINARY = 0;
//        public static final int THRESH_BINARY_INV = 1;
//        public static final int THRESH_TRUNC = 2;
//        public static final int THRESH_TOZERO = 3;
//        public static final int THRESH_TOZERO_INV = 4;
//        public static final int THRESH_MASK = 7;
//        public static final int THRESH_OTSU = 8;
//        public static final int THRESH_TRIANGLE = 16;

    @GetMapping(
        value = "/preparation",
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> showPreparedImage(
        @RequestParam(required = false, defaultValue = "true") boolean filtered,
        @RequestParam(required = false, defaultValue = "true") boolean gray,
        @RequestParam(required = false, defaultValue = "216")  int threshold,
        @RequestParam(required = false, defaultValue = "1000")  int maxValue,
        @RequestParam(required = false, defaultValue = "0")  int threshMethod
        ) throws IOException {
        return ResponseEntity.ok(imageService.createPreparedImage(gray, filtered, threshold, maxValue, threshMethod));
    }

    @GetMapping(
        value = "/findPositions",
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> findPositions(
        @RequestParam(required = false, defaultValue = "true") boolean filtered,
        @RequestParam(required = false, defaultValue = "true") boolean gray,
        @RequestParam(required = false, defaultValue = "216")  int threshold,
        @RequestParam(required = false, defaultValue = "1000")  int maxValue,
        @RequestParam(required = false, defaultValue = "0")  int threshMethod,
        @RequestParam(required = false, defaultValue = "4000000")  int matchingThreshold
    ) throws IOException {
        return ResponseEntity.ok(imageService.findPositions(gray, filtered, threshold, maxValue, threshMethod, matchingThreshold));
    }

    @PostMapping(value = "/uploadOfficeMap")
    public ResponseEntity<Integer> uploadOfficeMap(@RequestParam("file") MultipartFile file, ModelMap modelMap) throws IOException {
        modelMap.addAttribute("file", file);
        return ResponseEntity.ok(imageService.storePosition(file.getBytes()));
    }
}
