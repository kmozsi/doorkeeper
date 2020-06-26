package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.data.OfficePositionOrientation;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static com.karanteam.doorkeeper.data.OfficePositionOrientation.NORTH;

@Service
@Slf4j
public class OfficeMapService {

    private static final String BASE_PATH = "classpath:image/";

    private final OfficePositionService officePositionService;

    public OfficeMapService(OfficePositionService officePositionService) {
        this.officePositionService = officePositionService;
        OpenCV.loadShared();
    }

    public int storePositions(byte[] bytes) throws IOException {
        Mat uploadedMat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
        File chairFile = readImage("chair_binary.png");
        Mat chairMat = readFileToMat(chairFile);

        Mat preparedOffice = prepareImage(uploadedMat, true, true, 216, 1000, 0);
        Mat preparedChair = prepareImage(chairMat, true, true, 216, 1000, 0);

        Mat positions = findPositions(preparedOffice, preparedChair, 4000000);

        List<OfficePosition> officePositions = readPositions(positions, NORTH);

        officePositionService.setPositions(officePositions);

        return officePositions.size();
    }

    public byte[] findPositions(boolean gray, boolean filtered, int threshold, int maxValue, int threshMethod, int matchingThreshold) throws IOException {
        File officeFile = readImage("original.jpg");
        File chairFile = readImage("chair_binary.png");

        Mat officeMat = readFileToMat(officeFile);
        Mat chairMat = readFileToMat(chairFile);
        Mat originalMat = officeMat.clone();

        Mat preparedOffice = prepareImage(officeMat, gray, filtered, threshold, maxValue, threshMethod);
        Mat preparedChair = prepareImage(chairMat, gray, filtered, threshold, maxValue, threshMethod);

        Mat positions = findPositions(preparedOffice, preparedChair, matchingThreshold);

        log.info("Number of found positions: " + (int)positions.size().height);

        drawFoundPositions(originalMat, positions);

        return writeMatToImage(originalMat);
    }

    private Mat findPositions(Mat preparedOffice, Mat preparedChair, int matchingThreshold) {
        Mat matchingMat = new Mat();
        Imgproc.matchTemplate(preparedOffice, preparedChair, matchingMat, Imgproc.TM_CCOEFF);
        Imgproc.threshold(matchingMat, matchingMat, matchingThreshold, Double.MAX_VALUE, Imgproc.THRESH_TOZERO);
        Mat positions = new Mat();
        Core.findNonZero(matchingMat, positions);
        return positions;
    }

    private List<OfficePosition> readPositions(Mat positions, OfficePositionOrientation officePositionOrientation) {
        return IntStream.range(0, (int)positions.size().height).mapToObj(
            index -> {
                double[] coords = positions.get(index, 0);
                int x = (int)coords[0];
                int y = (int)coords[1];
                return OfficePosition.builder().x(x).y(y).orientation(officePositionOrientation).build();
            }
        ).collect(Collectors.toList());
    }

    private void drawFoundPositions(Mat originalMat, Mat positions) {
        IntStream.range(0, (int)positions.size().height).forEach(
            index -> {
                double[] coords = positions.get(index, 0);
                Rect rect = new Rect((int)coords[0], (int)coords[1], 20, 20);
                Imgproc.rectangle(originalMat, rect, new Scalar(100,0,200));
            }
        );
    }

    public byte[] createPreparedImage(boolean gray, boolean filtered, int threshold, int maxValue, int threshMethod) throws IOException {
        File officeFile = readImage("original.jpg");
        Mat officeMat = readFileToMat(officeFile);
        Mat preparedImage = prepareImage(officeMat, gray, filtered, threshold, maxValue, threshMethod);
        return writeMatToImage(preparedImage);
    }

    private Mat prepareImage(Mat mat, boolean gray, boolean filtered, int threshold, int maxValue, int threshMethod) {
        if (gray) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 0);
        }

        if (filtered) {
            Imgproc.threshold(mat, mat, threshold, maxValue, threshMethod);
        }
        return mat;
    }

    private File readImage(String imageName) throws FileNotFoundException {
        return ResourceUtils.getFile(BASE_PATH + imageName);
    }

    private byte[] writeMatToImage(Mat mat) {
        MatOfByte outputBytes = new MatOfByte();
        Imgcodecs.imencode(".png", mat, outputBytes);
        return outputBytes.toArray();
    }

    private Mat readFileToMat(File imageFile) throws IOException {
        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
    }
}




