package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.ApplicationConfig;
import com.karanteam.doorkeeper.data.ImageProcessingConfiguration;
import com.karanteam.doorkeeper.data.OfficePositionOrientation;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.Color;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import static com.karanteam.doorkeeper.service.ImageService.YELLOw;

@Service
@Slf4j
public class OfficeMapService {

    private final OfficePositionService officePositionService;
    private final ImageService imageService;
    private final ApplicationConfig applicationConfig;

    public OfficeMapService(
        OfficePositionService officePositionService,
        ImageService imageService,
        ApplicationConfig applicationConfig
    ) {
        this.officePositionService = officePositionService;
        this.imageService = imageService;
        this.applicationConfig = applicationConfig;
    }

    @PostConstruct
    public void init() throws IOException {
        OpenCV.loadShared();
        storePositions(Files.readAllBytes(imageService.readImage("original.jpg").toPath()));
    }

    public int storePositions(byte[] bytes) throws IOException {
        Mat chairMat = imageService.readMat("chair_binary.png");
        Mat uploadedMat = imageService.loadMat(bytes);
        Mat originalMat = uploadedMat.clone();

        Mat preparedOffice = prepareImage(uploadedMat);
        Mat preparedChair = prepareImage(chairMat);

        List<OfficePosition> officePositions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Mat rotatedChair = imageService.rotate(preparedChair, i * 90);
            Mat positions = findPositions(preparedOffice, rotatedChair, applicationConfig.getImage().getMatchingThreshold());
            officePositions.addAll(readFreePositions(originalMat, positions, OfficePositionOrientation.getByRotations(i)));
        }

        officePositionService.setPositions(officePositions);
        return officePositions.size();
    }

    public Mat findPositions(Mat preparedOffice, Mat preparedChair, int matchingThreshold) {
        Mat matchingMat = new Mat();
        Imgproc.matchTemplate(preparedOffice, preparedChair, matchingMat, Imgproc.TM_CCOEFF);
        Imgproc.threshold(matchingMat, matchingMat, matchingThreshold, Double.MAX_VALUE, Imgproc.THRESH_TOZERO);
        Mat positions = new Mat();
        Core.findNonZero(matchingMat, positions);
        return positions;
    }

    private List<OfficePosition> readFreePositions(Mat originalMat, Mat positions, OfficePositionOrientation officePositionOrientation) {
        final List<OfficePosition> officePositions = new ArrayList<>();
        IntStream.range(0, (int)positions.size().height).forEach(
            index -> {
                double[] coords = positions.get(index, 0);
                int x = (int)coords[0];
                int y = (int)coords[1];
                Rect rect = new Rect(x, y, 20, 20);
                Scalar scalar = Core.mean(originalMat.submat(rect));
                if (Color.GREEN.match(scalar, 35)) {
                    officePositions.add(OfficePosition.builder().x(x).y(y).status(PositionStatus.FREE).orientation(officePositionOrientation).build());
                }
            }
        );
        return officePositions;
    }

    private Mat prepareImage(Mat mat) {
        ImageProcessingConfiguration imageConfig = applicationConfig.getImage();

        return imageService.prepareImage(
            mat,
            imageConfig.getGrayScale(),
            imageConfig.getThreshold(),
            imageConfig.getMaxValue(),
            imageConfig.getThresholdMethod()
        );
    }

    public byte[] markPosition(OfficePosition position) throws IOException {
        Mat officeMat = imageService.readMat("original.jpg"); // TODO current office pic
        imageService.markPosition(officeMat, position.getX(), position.getY(), YELLOw);
        return imageService.writeMatToImage(officeMat);
    }
}
