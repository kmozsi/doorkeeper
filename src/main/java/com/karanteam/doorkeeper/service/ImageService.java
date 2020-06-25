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
import java.util.ArrayList;
import java.util.List;

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

    public byte[] findPositions() throws IOException {
        File officeFile = readImage("office_cut_2.png");
        File chairFile = readImage("chair_contour_2.png");

        Mat officeMat = readFileToMat(officeFile);
        Mat chairMat = readFileToMat(chairFile);

        int method = Imgproc.TM_CCOEFF;
//        int method = Imgproc.TM_CCOEFF_NORMED;
//        int method = Imgproc.TM_CCORR;
//        int method = Imgproc.TM_CCORR_NORMED;
//        int method = Imgproc.TM_SQDIFF;
//        int method = Imgproc.TM_SQDIFF_NORMED;

        Mat resultMat = positionMatching(officeMat, chairMat, method);
        return writeMatToImage(resultMat);
    }

    public byte[] processImage(boolean gray, PositionConfiguration configuration) throws IOException {
        File officeFile = readImage("office_cut.png");
        File chairFile = readImage("chair_contour_2.png");

        Mat officeMat = readFileToMat(officeFile);
        Mat chairMat = readFileToMat(chairFile);

        Imgproc.cvtColor(chairMat, chairMat, Imgproc.COLOR_BGRA2BGR);

        configuration.getPositions()
            .forEach(position -> insertPictureInPicture(officeMat, chairMat, position.getX(), position.getY()));

        Mat resultPicture = officeMat.clone();
        if (gray) {
            Imgproc.cvtColor(officeMat, resultPicture, Imgproc.COLOR_RGB2GRAY, 0);
        }
        return writeMatToImage(resultPicture);
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

    private Mat positionMatching(Mat office, Mat position, int method) {
        Mat result = new Mat();

        Mat officeTransformedMat = new Mat();
        Mat positionTransformedScaledMat = new Mat();
        Imgproc.cvtColor(office, officeTransformedMat, Imgproc.COLOR_RGB2GRAY, 0);
        Imgproc.cvtColor(position, positionTransformedScaledMat, Imgproc.COLOR_RGB2GRAY, 0);
        Imgproc.matchTemplate(officeTransformedMat, positionTransformedScaledMat, result, method);

        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result);

        Rect rect = new Rect((int)minMaxLocResult.maxLoc.x, (int)minMaxLocResult.maxLoc.y, 20, 20);
        Imgproc.rectangle(office, rect, new Scalar(100,0,200));

        return office;
    }
}




