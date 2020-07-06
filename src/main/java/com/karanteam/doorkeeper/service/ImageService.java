package com.karanteam.doorkeeper.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import static org.opencv.core.CvType.CV_8UC3;

@Service
public class ImageService {
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

    public void markPosition(Mat mat, int x, int y, Size size, Scalar color) {
        Rect rect = new Rect(x, y, (int) size.width, (int) size.height);
        Imgproc.rectangle(mat, rect, color);
    }

    public void fillPosition(Mat mat, int x, int y, Size size, Scalar color, int threshold, int proc, int max) {
        Mat mask = getMask(mat, x, y, size, threshold, proc, max);
        fillColor(mat, mask, x, y, size, color);
    }

    public Mat getMask(Mat original, int x, int y, Size size, int threshold, int proc, int max) {
        Mat mask = original.submat(y, y + (int) size.height, x, x + (int) size.width);
        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2BGR);
        Imgproc.threshold(mask, mask, threshold, max, proc);
        return mask;
    }

    public void fillColor(Mat original, Mat mask, int x, int y, Size size, Scalar color) {
        Mat red = new Mat((int) size.height, (int) size.width, CV_8UC3, color);

        red.copyTo(original.rowRange(
            y, y + red.height()
        ).colRange(
            x, x + red.width()
        ), mask);
    }

    public File readImage(String imageName) throws FileNotFoundException {
        return ResourceUtils.getFile(BASE_PATH + imageName);
    }
}
