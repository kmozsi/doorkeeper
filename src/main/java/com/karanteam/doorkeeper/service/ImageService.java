package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.data.Office;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ImageService {

    public ImageService() {
        OpenCV.loadShared();
    }

    public Office scanOffice(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        return Office.builder().dimension(new Dimension(image.getWidth(), image.getHeight())).build();
    }

    public Office scanOfficeWithOpenCV(File imageFile) throws IOException {
        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        Mat mat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
        return Office.builder().dimension(new Dimension(mat.width(), mat.height())).build();
    }
}




