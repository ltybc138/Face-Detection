package com.denis.detection.cascadeDetecting;

import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {
    private CascadeClassifier cascadeDefiner;

    public void detectAndDrawFace(Mat image) {
        MatOfRect faceDetections = new MatOfRect();
        cascadeDefiner.detectMultiScale(image, faceDetections);
        for (Rect rect : faceDetections.toArray()) {
            Core.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }
    }

    public void detectAndDrawEyes(Mat image) {
        MatOfRect eyesDetections = new MatOfRect();
        cascadeDefiner.detectMultiScale(image, eyesDetections);
        for (Rect rect : eyesDetections.toArray()) {
            Core.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }
    }

    public void detectAndDrawNose(Mat image) {
        MatOfRect noseDetections = new MatOfRect();
        cascadeDefiner.detectMultiScale(image, noseDetections);
        for (Rect rect : noseDetections.toArray()) {
            Core.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }
    }

    public void detectAndDrawMouth(Mat image) {
        MatOfRect mouthDetections = new MatOfRect();
        cascadeDefiner.detectMultiScale(image, mouthDetections);
        for (Rect rect : mouthDetections.toArray()) {
            Core.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }
    }
    // TODO add more cascades

    public void loadCascade(String cascadePath) {
        cascadeDefiner = new CascadeClassifier(cascadePath);
    }
}
