package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.data.Office;
import com.karanteam.doorkeeper.data.PositionConfiguration;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.IntStream;

@Service
public class ImageService {

    private static final String BASE_PATH = "/home/gincsait/Dokumentumok/doorkeeper/";

    public ImageService() {
        OpenCV.loadShared();
    }

    public Office scanOffice(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        return Office.builder().dimension(new Dimension(image.getWidth(), image.getHeight())).build();
    }

    public byte[] findPositions(boolean gray, boolean filtered, int threshold, int maxValue, int threshMethod, int matchingThreshold) throws IOException {
        File officeFile = readImage("office_cut_2.png");
        Mat officeMat = readFileToMat(officeFile);
        Mat originalMat = officeMat.clone();

        Mat preparedMat = prepareImage(officeMat, gray, filtered, threshold, maxValue, threshMethod);

        File chairFile = readImage("chair_binary.png");
        Mat chairMat = readFileToMat(chairFile);

        Mat preparedChair = prepareImage(chairMat, gray, filtered, threshold, maxValue, threshMethod);

        int method = Imgproc.TM_CCOEFF;
//        int method = Imgproc.TM_CCOEFF_NORMED;
//        int method = Imgproc.TM_CCORR;
//        int method = Imgproc.TM_CCORR_NORMED;
//        int method = Imgproc.TM_SQDIFF;
//        int method = Imgproc.TM_SQDIFF_NORMED;
        Mat result = new Mat();
        Imgproc.matchTemplate(preparedMat, preparedChair, result, method);

        Imgproc.threshold(result, result, matchingThreshold, Double.MAX_VALUE, Imgproc.THRESH_TOZERO);

        Mat positions = new Mat();
        Core.findNonZero(result, positions);

        IntStream.range(0, (int)positions.size().height).forEach(
            index -> {
                double[] coords = positions.get(index, 0);
                Rect rect = new Rect((int)coords[0], (int)coords[1], 20, 20);
                Imgproc.rectangle(originalMat, rect, new Scalar(100,0,200));
            }
        );

//        Mat resultMat = positionMatching(preparedMat, chairMat, method, matchingThreshold);
        return writeMatToImage(originalMat);
    }

    //        public static final int THRESH_BINARY = 0;
//        public static final int THRESH_BINARY_INV = 1;
//        public static final int THRESH_TRUNC = 2;
//        public static final int THRESH_TOZERO = 3;
//        public static final int THRESH_TOZERO_INV = 4;
//        public static final int THRESH_MASK = 7;
//        public static final int THRESH_OTSU = 8;
//        public static final int THRESH_TRIANGLE = 16;
    private Mat prepareImage(Mat mat, boolean gray, boolean filtered, int threshold, int maxValue, int threshMethod) throws IOException {
        if (gray) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 0);
        }

        if (filtered) {
            Imgproc.threshold(mat, mat, threshold, maxValue, threshMethod);
        }

        return mat;
    }

    public byte[] processImage(boolean gray, boolean filtered, PositionConfiguration configuration, int threshold, int maxValue, int threshMethod) throws IOException {
        File officeFile = readImage("office_cut.png");
        File chairFile = readImage("chair_contour_2.png");

        Mat officeMat = readFileToMat(officeFile);
        Mat chairMat = readFileToMat(chairFile);

        Imgproc.cvtColor(chairMat, chairMat, Imgproc.COLOR_BGRA2BGR);

        configuration.getPositions()
            .forEach(position -> insertPictureInPicture(officeMat, chairMat, position.getX(), position.getY()));

        if (gray) {
            Imgproc.cvtColor(officeMat, officeMat, Imgproc.COLOR_RGB2GRAY, 0);
        }

        if (filtered) {
            Imgproc.threshold(officeMat, officeMat, threshold, maxValue, threshMethod);
        }
        return writeMatToImage(officeMat);
    }

    private void insertPictureInPicture(Mat original, Mat insert, int x, int y) {
        insert.copyTo(original.rowRange(
            y, y + insert.height()
        ).colRange(
            x, x + insert.width()
        ));
    }

    private File readImage(String imageName) throws FileNotFoundException {
        return ResourceUtils.getFile(BASE_PATH + imageName);
    }

    private byte[] writeMatToImage(Mat mat) {
        MatOfByte outputBytes = new MatOfByte();
        Imgcodecs.imencode(".png", mat, outputBytes);
        return outputBytes.toArray();
    }

    public Office scanOfficeWithOpenCV(File imageFile) throws IOException {
        Mat mat = readFileToMat(imageFile);
        return Office.builder().dimension(new Dimension(mat.width(), mat.height())).build();
    }

    private Mat readFileToMat(File imageFile) throws IOException {
        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
    }

    private Mat positionMatching(Mat office, Mat position, int method, int threshold) {
        Mat result = new Mat();

        Mat officeTransformedMat = new Mat();
        Mat positionTransformedScaledMat = new Mat();
        Imgproc.cvtColor(office, officeTransformedMat, Imgproc.COLOR_RGB2GRAY, 0);
        Imgproc.cvtColor(position, positionTransformedScaledMat, Imgproc.COLOR_RGB2GRAY, 0);
        Imgproc.matchTemplate(officeTransformedMat, positionTransformedScaledMat, result, method);

        Imgproc.threshold(result, result, threshold, Double.MAX_VALUE, Imgproc.THRESH_TOZERO);

        Mat positions = new Mat();
        Core.findNonZero(result, positions);

//        IntStream.range(0, positions.)
        IntStream.range(0, (int)positions.size().height).forEach(
            index -> {
                double[] coords = positions.get(index, 0);
                Rect rect = new Rect((int)coords[0], (int)coords[1], 20, 20);
                Imgproc.rectangle(office, rect, new Scalar(100,0,200));
            }
        );

        return office;
    }
}




