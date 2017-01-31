package com.denis.detection.cascadeDetecting;

import com.denis.detection.image.Utils;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FaceDetector {
    public static final String faceDefaultCascadePath = "res/cascades/frontal_face/haarcascade_frontalface_default.xml";
    public static final String faceAltCascadePath = "res/cascades/frontal_face/haarcascade_frontalface_alt.xml";
    public static final String faceAlt2CascadePath = "res/cascades/frontal_face/haarcascade_frontalface_alt2.xml";
    public static final String faceAltTreeCascadePath = "res/cascades/frontal_face/haarcascade_frontalface_alt_tree.xml";

    private CascadeClassifier cascadeDefiner;
    private Mat faceRectMat = null;
    private int faceCounter = 0;

    public FaceDetector(int faceCounter) {
        this.faceCounter = faceCounter;
    }

    public void detectAndDrawFace(Mat src, Mat dst, int pyrCount) {
        MatOfRect faceDetections = new MatOfRect();
        cascadeDefiner.detectMultiScale(src, faceDetections);
        Rect faceRect = null;
        for (Rect rect : faceDetections.toArray()) {
            Core.rectangle(dst, new Point(rect.x * pyrCount, rect.y * pyrCount),
                    new Point(rect.x * pyrCount + rect.width * pyrCount, rect.y * pyrCount + rect.height * pyrCount),
                    new Scalar(0, 255, 0));
            faceRect = new Rect(rect.x, rect.y, rect.width, rect.height);
        }
        faceRectMat = new Mat(src, faceRect);
        this.faceCounter++;
        Highgui.imwrite("FacesBuffer/faceRect.jpg", faceRectMat);
        /*File faceRectFile = new File("FacesBuffer/faceRect-" + faceCounter + ".jpg");
        BufferedImage faceImage = Utils.matToBufferedImage(faceMat);
        try {
            ImageIO.write(faceImage, "JPEG", faceRectFile);
            System.out.println("Face rect saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void loadCascade(String cascadePath) {
        cascadeDefiner = new CascadeClassifier(cascadePath);
    }

    public Mat getFaceRect() {
        return this.faceRectMat;
    }
}
