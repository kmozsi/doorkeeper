package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.Color;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static com.karanteam.doorkeeper.enumeration.Color.RED;
import static com.karanteam.doorkeeper.enumeration.Color.YELLOW;
import static org.opencv.core.CvType.CV_8UC3;

@Service
@Slf4j
public class ImageProcessingTuningService {

    private final ImageService imageService;
    private final OfficeMapService officeMapService;
    private final OfficePositionService officePositionService;

    public ImageProcessingTuningService(ImageService imageService, OfficeMapService officeMapService, OfficePositionService officePositionService) {
        this.imageService = imageService;
        this.officeMapService = officeMapService;
        this.officePositionService = officePositionService;
    }

    public byte[] createPreparedImage(boolean gray, int threshold, int maxValue, int threshMethod) throws IOException {
        Mat officeMat  = imageService.readMat("original.jpg");
        Mat preparedImage = imageService.prepareImage(officeMat, gray, threshold, maxValue, threshMethod);
        return imageService.writeMatToImage(preparedImage);
    }

    public byte[] findPositions(boolean gray, int threshold, int maxValue, int threshMethod, int matchingThreshold) throws IOException {
        Mat officeMat = imageService.readMat("original.jpg");
        Mat chairMat = imageService.readMat("chair_binary.png");

        Mat originalMat = officeMat.clone();
        Mat destinationMat = officeMat.clone();

        Mat preparedOffice = imageService.prepareImage(officeMat, gray, threshold, maxValue, threshMethod);
        Mat preparedChair = imageService.prepareImage(chairMat, gray, threshold, maxValue, threshMethod);

        for (int i = 0; i < 4; i++) {
            Mat rotatedChair = imageService.rotate(preparedChair, i * 90);
            Mat positions = officeMapService.findPositions(preparedOffice, rotatedChair, matchingThreshold);
            log.info("Number of found positions: " + (int)positions.size().height);
            drawFoundPositions(originalMat, destinationMat, positions);
        }

        return imageService.writeMatToImage(destinationMat);
    }

    private void drawFoundPositions(Mat originalMat, Mat destinationMat, Mat positions) {
        IntStream.range(0, (int)positions.size().height).forEach(
            index -> {
                double[] coords = positions.get(index, 0);
                Rect rect = new Rect((int)coords[0], (int)coords[1], 20, 20);

                Scalar scalar = Core.mean(originalMat.submat(rect));
                if (Color.GREEN.match(scalar, 35)) {
                    Imgproc.rectangle(destinationMat, rect, new Scalar(0,220,220));
                } else {
                    Imgproc.rectangle(destinationMat, rect, new Scalar(200,0,200));
                }
            }
        );
    }

    public byte[] getLayout(int threshold, int proc, int max, boolean justMask) throws IOException {
        Mat officeMat = imageService.readMat("original.jpg"); // TODO current office pic
        List<OfficePosition> allPositions = officePositionService.getAllPositions();
        allPositions.forEach(position -> fillPosition(
            officeMat,
            position.getX(),
            position.getY(),
            position.getOrientation().getSize(),
            RED.getColor(),

            threshold,
            proc,
            max,
            justMask

            )
        );
        return imageService.writeMatToImage(officeMat);
    }

    public void fillPosition(Mat mat, int x, int y, Size size, Scalar color, int threshold, int proc, int max, boolean justMask) {
        if (justMask) {
            Mat mask = imageService.getMask(mat, x, y, size, threshold, proc, max);
            insertPicture(mat, mask, x, y);
        } else {
            imageService.fillPosition(mat, x, y, size, color, threshold, proc, max);
        }
    }

    private void insertPicture(Mat original, Mat picture, int x, int y) {
        picture.copyTo(original.rowRange(
            y, y + picture.height()
        ).colRange(
            x, x + picture.width()
        ));
    }

}
