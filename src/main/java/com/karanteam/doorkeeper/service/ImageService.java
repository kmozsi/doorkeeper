package com.karanteam.doorkeeper.service;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ImageService {

    public static final Scalar YELLOw = new Scalar(0,220,220);
    private static final String BASE_PATH = "classpath:image/";

    public Mat loadMat(byte[] bytes) {
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
    }

    public Mat prepareImage(Mat mat, boolean grayScale, int threshold, int maxValue, int threshMethod) {
        if (grayScale) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 0);
        }
        Imgproc.threshold(mat, mat, threshold, maxValue, threshMethod);
        return mat;
    }

    public byte[] writeMatToImage(Mat mat) {
        MatOfByte outputBytes = new MatOfByte();
        Imgcodecs.imencode(".png", mat, outputBytes);
        return outputBytes.toArray();
    }

    public Mat readFileToMat(File imageFile) throws IOException {
        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
    }

    public Mat rotate(final Mat original, final int angle) {
        final int rotationCode = ((angle % 360) / 90) - 1;
        if (rotationCode < 0) {
            return original;
        }
        Mat rotatedMat = original.clone();
        Core.rotate(rotatedMat, rotatedMat, rotationCode);
        return rotatedMat;
    }

    public void markPosition(Mat mat, int x, int y, Scalar color) {
        Rect rect = new Rect(x, y, 20, 20);
        Imgproc.rectangle(mat, rect, color);
    }

    public Mat readMat(String imageName) throws IOException {
        return readFileToMat(readImage(imageName));
    }

    public File readImage(String imageName) throws FileNotFoundException {
        return ResourceUtils.getFile(BASE_PATH + imageName);
    }

}
