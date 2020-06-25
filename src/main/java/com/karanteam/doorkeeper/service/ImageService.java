package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.data.Office;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    public ImageService() {
        OpenCV.loadShared();
    }

    public byte[] getImage() throws IOException {
        File imageFile = ResourceUtils.getFile("/home/gincsait/Dokumentumok/doorkeeper/office_cut.png");
//        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        return transform(imageFile);
    }

    public byte[] transform(File imageFile) throws IOException {
        Mat originalPicture = readFileToMat(imageFile);
        Mat resultPicture = new Mat();

//        Mat blurredImage = new Mat();
//        Mat hsvImage = new Mat();
//        Mat mask = new Mat();
//        Mat morphOutput = new Mat();
//
//        Imgproc.blur(originalPicture, blurredImage, new Size(7, 7));
//        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

//        Imgproc.cvtColor(originalPicture, grayPicture, Imgproc.COLOR_RGB2GRAY, 0);

        Imgproc.cvtColor(originalPicture, resultPicture, Imgproc.COLOR_RGB2GRAY, 0);


        MatOfByte outputBytes = new MatOfByte();
        Imgcodecs.imencode(".png", resultPicture, outputBytes);
        return outputBytes.toArray();
    }

    public Office scanOffice(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        return Office.builder().dimension(new Dimension(image.getWidth(), image.getHeight())).build();
    }

    public Office scanOfficeWithOpenCV(File imageFile) throws IOException {
        Mat mat = readFileToMat(imageFile);
        return Office.builder().dimension(new Dimension(mat.width(), mat.height())).build();
    }

    private Mat readFileToMat(File imageFile) throws IOException {
        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
    }

    public int findTables(File imageFile) throws IOException {
        Mat originalPicture = readFileToMat(imageFile);

        Mat blurredImage = new Mat();
        Mat hsvImage = new Mat();
        Mat mask = new Mat();
        Mat morphOutput = new Mat();

// remove some noise
        Imgproc.blur(originalPicture, blurredImage, new Size(7, 7));
// convert the frame to HSV
        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);




        // get thresholding values from the UI
// remember: H ranges 0-180, S and V range 0-255
        double hueStart = 10.0;
        double saturationStart = 10.0;
        double valueStart = 10.0;
        double hueStop = 100.0;
        double saturationStop = 200.0;
        double valueStop = 200.0;
        Scalar minValues = new Scalar(hueStart, saturationStart, valueStart);
        Scalar maxValues = new Scalar(hueStop, saturationStop, valueStop);

// show the current selected HSV range
        String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
            + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
            + minValues.val[2] + "-" + maxValues.val[2];
//        this.onFXThread(this.hsvValuesProp, valuesToPrint);

// threshold HSV image to select tennis balls
        Core.inRange(hsvImage, minValues, maxValues, mask);
// show the partial output
//        this.onFXThread(maskProp, this.mat2Image(mask));



        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
        {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
            {
//                Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
            }
        }


        return 0;
    }
}




