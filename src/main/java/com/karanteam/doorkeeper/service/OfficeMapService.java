package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.ApplicationConfig;
import com.karanteam.doorkeeper.data.ImageProcessingConfiguration;
import com.karanteam.doorkeeper.data.OfficePositionOrientation;
import com.karanteam.doorkeeper.entity.Image;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.Color;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
import com.karanteam.doorkeeper.repository.ImageRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import static com.karanteam.doorkeeper.enumeration.Color.YELLOW;

@Service
@Slf4j
public class OfficeMapService {

    private final OfficePositionService officePositionService;
    private final ImageService imageService;
    private final ApplicationConfig applicationConfig;
    private final ImageRepository imageRepository;
    private final ImageProcessingConfiguration imageProcessingConfiguration;
    private final AdminService adminService;

    public OfficeMapService(
        OfficePositionService officePositionService,
        ImageService imageService, ApplicationConfig applicationConfig,
        ImageRepository imageRepository, AdminService adminService) {
        this.officePositionService = officePositionService;
        this.imageService = imageService;
        this.applicationConfig = applicationConfig;
        this.imageRepository = imageRepository;
        this.imageProcessingConfiguration = applicationConfig.getImage();
        this.adminService = adminService;
    }

    @PostConstruct
    public void init() throws IOException {
        OpenCV.loadShared();
        storePositions(Files.readAllBytes(imageService.readImage("original.jpg").toPath()));
    }

    public int storePositions(byte[] bytes) throws IOException {
        storeNewOffice(bytes);
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

        int positionCount = officePositionService.setPositions(officePositions);
        adminService.setMapCapacity(positionCount);
        return positionCount;
    }

    private void storeNewOffice(byte[] content) {
        Image office = imageRepository.findByKey(Image.OFFICE).orElse(new Image());
        office.setKey(Image.OFFICE);
        office.setContent(content);
        imageRepository.save(office);
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

    private Mat getCurrentOfficeMat() {
        Image office = imageRepository.findByKey(Image.OFFICE).orElseThrow(
            () -> new RuntimeException("Cannot find office map!")
        );
        return imageService.loadMat(office.getContent());
    }

    public byte[] markPosition(OfficePosition position) {
        Mat officeMat = getCurrentOfficeMat();
        imageService.markPosition(
            officeMat,
            position.getX(),
            position.getY(),
            position.getOrientation().getSize(),
            YELLOW.getColor()
        );
        return imageService.writeMatToImage(officeMat);
    }

    public byte[] getLayout() {
        Mat officeMat = getCurrentOfficeMat();

        List<OfficePosition> allPositions = officePositionService.getAllPositions();
        allPositions.forEach(
            position -> fillWithColor(
                officeMat, position.getX(), position.getY(), position.getOrientation().getSize(), position.getStatus().getColor()
            )
        );

        return imageService.writeMatToImage(officeMat);
    }


    private void fillWithColor(Mat mat, int x, int y, Size size, Color color) {
        if (color != null) {
            imageService.fillPosition(
                mat, x, y, size, color.getColor(),
                imageProcessingConfiguration.getColoringThreshold(),
                imageProcessingConfiguration.getColoringThresholdMethod(),
                imageProcessingConfiguration.getColoringMaxValue()
            );
        }
    }
}
