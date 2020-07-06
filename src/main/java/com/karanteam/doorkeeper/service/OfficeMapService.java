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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import static com.karanteam.doorkeeper.enumeration.Color.YELLOW;

@Service
@Slf4j
public class OfficeMapService {

    private final static String MAP_IMAGE = "original.jpg";
    private final static String CHAIR_IMAGE = "chair_binary.png";

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
        storeImage(Image.CHAIR, readAllBytes(CHAIR_IMAGE));
        storeOfficeAndPositions(readAllBytes(MAP_IMAGE));
    }

    private byte[] readAllBytes(String imageName) throws IOException {
        return Files.readAllBytes(imageService.readImage(imageName).toPath());
    }

    public int storeOfficeAndPositions(byte[] bytes) {
        storeImage(Image.OFFICE, bytes);
        return storePositions(imageService.loadMat(bytes));
    }

    private void storeImage(String key, byte[] content) {
        Image office = imageRepository.findByKey(key).orElse(new Image());
        office.setKey(key);
        office.setContent(content);
        imageRepository.save(office);
    }

    private int storePositions(Mat uploadedMat) {
        Mat chairMat = getCurrentMat(Image.CHAIR);
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

    public void recalculatePositions() {
        storePositions(getCurrentMat(Image.OFFICE));
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
        IntStream.range(0, (int) positions.size().height).forEach(
            index -> {
                double[] coords = positions.get(index, 0);
                int x = (int) coords[0];
                int y = (int) coords[1];
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

    private Mat getCurrentMat(String imageKey) {
        Image img = imageRepository.findByKey(imageKey).orElseThrow(
            () -> new RuntimeException("Cannot find image with key " + imageKey)
        );
        return imageService.loadMat(img.getContent());
    }

    public byte[] markPosition(OfficePosition position) {
        Mat officeMat = getCurrentMat(Image.OFFICE);
        imageService.fillPosition(
            officeMat,
            position.getX(),
            position.getY(),
            position.getOrientation().getSize(),
            YELLOW.getColor(),
            imageProcessingConfiguration.getColoringThreshold(),
            imageProcessingConfiguration.getColoringThresholdMethod(),
            imageProcessingConfiguration.getColoringMaxValue()
        );
        imageService.markPosition(
            officeMat,
            position.getX(),
            position.getY(),
            position.getOrientation().getSize(),
            new Scalar(0.0, 0.0, 160.0)
        );
        return imageService.writeMatToImage(officeMat);
    }

    public byte[] getLayout() {
        Mat officeMat = getCurrentMat(Image.OFFICE);

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
