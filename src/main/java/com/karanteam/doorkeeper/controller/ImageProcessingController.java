package com.karanteam.doorkeeper.controller;

import com.karanteam.doorkeeper.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class ImageProcessingController {

    private final ImageService imageService;

    public ImageProcessingController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping(
        value = "/picture",
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> printDeliveryNote(
        @RequestParam(required = false, defaultValue = "false") boolean gray,
        @RequestParam int x,
        @RequestParam int y
    ) throws IOException {
        return ResponseEntity.ok(imageService.processImage(gray, x, y));
    }

}
