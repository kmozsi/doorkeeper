package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.data.Office;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

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

    public byte[] processImageWithBufferedImage(boolean gray, int x, int y) throws IOException {
        File officeFile = readImage("office_cut.png");
        File chairFile = readImage("chair_contour_2.png");

        BufferedImage officeImage = ImageIO.read(officeFile);
        BufferedImage chairImage = ImageIO.read(chairFile);

        WritableRaster raster = chairImage.getRaster();


        int[] data = new int[30 * 40];
//        data[10] = 0;
//        data[11] = 200;
//        data[12] = 7;
//        data[13] = 250;


        WritableRaster newRaster = raster.createCompatibleWritableRaster(new Rectangle(x,y,30,40));
        newRaster.setDataElements(0, 0, new byte[10]);

        officeImage.setData(newRaster);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(officeImage, "png", baos);
        return baos.toByteArray();
    }

    public byte[] processImage(boolean gray, int x, int y) throws IOException {
        File officeFile = readImage("office_cut.png");
        File chairFile = readImage("chair_contour_2.png");

        Mat officeMat = readFileToMat(officeFile);
        Mat chairMat = readFileToMat(chairFile);

        officeMat = insertPictureInPicture(officeMat, chairMat, x, y);
//        officeMat = addWeighted(officeMat, officeMat);

        Mat resultPicture = officeMat.clone();

        if (gray) {
            Imgproc.cvtColor(officeMat, resultPicture, Imgproc.COLOR_RGB2GRAY, 0);
        }

        return writeMatToImage(resultPicture);
    }

    private Mat insertPictureInPicture(Mat original, Mat insert, int x, int y) {

        Mat src = new Mat( 25,  50, CV_8UC3, new Scalar(200, 1, 1)); // 5x7

        src.copyTo(original.rowRange(
            y, y+src.height()
        ).colRange(
            x, x+src.width()
        ));

        return original;
    }

    private File readImage(String imageName) throws FileNotFoundException {
        return ResourceUtils.getFile(BASE_PATH + imageName);
    }

    private byte[] writeMatToImage(Mat mat) {
        MatOfByte outputBytes = new MatOfByte();
        Imgcodecs.imencode(".png", mat, outputBytes);
        return outputBytes.toArray();
    }


    public byte[] transform(File imageFile) throws IOException {
//        Imgproc.blur(originalPicture, blurredImage, new Size(7, 7));
//        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
//        Imgproc.cvtColor(originalPicture, grayPicture, Imgproc.COLOR_RGB2GRAY, 0);
        return null;
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




