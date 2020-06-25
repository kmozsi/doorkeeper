package com.karanteam.doorkeeper.service;


import com.karanteam.doorkeeper.data.Office;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

@SpringBootTest
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Test
    public void testScanOffice() throws IOException {
        File file = ResourceUtils.getFile("classpath:image/rectangles.png");
        Office office = imageService.scanOffice(file);
        Assertions.assertEquals(new Dimension(237, 422), office.getDimension());
    }

    @Test
    public void testScanOfficeWithOpenCV() throws IOException {
        File file = ResourceUtils.getFile("classpath:image/rectangles.png");
        Office office = imageService.scanOfficeWithOpenCV(file);
        Assertions.assertEquals(new Dimension(237, 422), office.getDimension());
    }

//    @Test
//    public void testFindTables() throws IOException {
//        File file = ResourceUtils.getFile("classpath:image/rectangles.png");
//        int numberOfTables = imageService.findTables(file);
//        Assertions.assertEquals(3, numberOfTables);
//    }
}
