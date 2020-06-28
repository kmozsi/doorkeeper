package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.enumeration.Color;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.stream.IntStream;

@Service
@Slf4j
public class ImageProcessingTuningService {

    private final ImageService imageService;
    private final OfficeMapService officeMapService;

    public ImageProcessingTuningService(ImageService imageService, OfficeMapService officeMapService) {
        this.imageService = imageService;
        this.officeMapService = officeMapService;
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
}
