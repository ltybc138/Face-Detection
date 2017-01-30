package com.denis.detection.image;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ImageTransformations {

    public static void getTransformedMat(Mat src, Mat dst) {
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        Imgproc.pyrDown(src, dst);
        Imgproc.pyrDown(src, dst);
    }
}
