package com.denis.detection.controllers;

import com.denis.detection.cascadeDetecting.FaceDetector;
import com.denis.detection.image.ImageTransformations;
import com.denis.detection.image.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CamWindowController {

    // cam javafx items
    @FXML
    private Button startCam;
    @FXML
    private ImageView camView;

    // cascades radio buttons
    @FXML
    private RadioButton defaultCascade;
    @FXML
    private RadioButton altCascade;
    @FXML
    private RadioButton alt2Cascade;
    @FXML
    private RadioButton altTreeCascade;
    @FXML
    private CheckBox detectFaceCheckBox;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive;
    // now uses filter
    protected boolean firstInto;

    // face rect
    @FXML
    private Button grabFaceRectButton;
    private int faceCounter = 0;
    private FaceDetector detector = null;

    public CamWindowController() {
        cameraActive = false;
        firstInto = false;
    }

    @FXML
    protected void startCam() {
        firstInto = true;
        defaultCascade.setSelected(true);
        setAllRadioCascadesDisable();
        detectFaceCheckBox.setSelected(false);
        if (!this.cameraActive) {
            log("Start button have been touched");

            // start the video capture
            int cameraId = 0;
            this.capture.open(cameraId);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                log("Starting cam thread...");

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = () -> {
                    // effectively grab and process a single frame
                    Mat frame = grabFrame();

                    // convert and show the frame
                    Image imageToShow = Utils.mat2Image(frame);
                    updateImageView(camView, imageToShow);
                };
                // init timer
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 15, TimeUnit.MILLISECONDS);

                // update the button content
                this.startCam.setText("Stop Camera");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.startCam.setText("Start Camera");
            // stop the timer
            log("Start stopping cam thread...");
            this.stopAcquisition();
            log("Cam thread stopped");
        }
    }

    private Mat grabFrame() {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    Mat copyOfFrame = new Mat();
                    frame.copyTo(copyOfFrame);
                    ImageTransformations.getTransformedMat(copyOfFrame, copyOfFrame);
                    loadFaceRect(copyOfFrame, frame, 4);
                }
            } catch (Exception e) {
                log("Exception: " + e);
            }
        }
        return frame;
    }

    private void loadFaceRect(Mat src, Mat dst, int pyrCount) {
        this.detector = new FaceDetector(faceCounter);
        if (detectFaceCheckBox.isSelected()) {
            if (!defaultCascade.isSelected()) {
                detector.loadCascade(FaceDetector.faceDefaultCascadePath);
                detector.detectAndDrawFace(src, dst, pyrCount);
            } else if (!altCascade.isSelected()) {
                detector.loadCascade(FaceDetector.faceAltCascadePath);
                detector.detectAndDrawFace(src, dst, pyrCount);
            } else if (!alt2Cascade.isSelected()) {
                detector.loadCascade(FaceDetector.faceAlt2CascadePath);
                detector.detectAndDrawFace(src, dst, pyrCount);
            } else if (!altTreeCascade.isSelected()) {
                detector.loadCascade(FaceDetector.faceAltTreeCascadePath);
                detector.detectAndDrawFace(src, dst, pyrCount);
            }
        }
    }

    private void setAllRadioCascadesOff() {
        this.defaultCascade.setSelected(false);
        this.altCascade.setSelected(false);
        this.alt2Cascade.setSelected(false);
        this.altTreeCascade.setSelected(false);
    }

    private void setAllRadioCascadesDisable() {
        this.defaultCascade.setDisable(true);
        this.altCascade.setDisable(true);
        this.alt2Cascade.setDisable(true);
        this.altTreeCascade.setDisable(true);
    }

    private void setAllRadioCascadesEnable() {
        this.defaultCascade.setDisable(false);
        this.altCascade.setDisable(false);
        this.alt2Cascade.setDisable(false);
        this.altTreeCascade.setDisable(false);
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log("Exception: " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    private void saveGrabbedFaceRect(Mat faceRect) {
        Highgui.imwrite("FacesBuffer/" + "face-" + faceCounter + ".jpg" , faceRect);
        log("Face rect saved successfully");
        faceCounter++;
    }

    protected void log(String logMessage) {
        System.out.println(logMessage);
    }

    @FXML
    protected void detectFaceAction() {
        detectFaceCheckBox.setOnAction(e -> {
            if (detectFaceCheckBox.isSelected()) {
                setAllRadioCascadesEnable();
                //grabFaceRectButton.setDisable(false);
            } else {
                setAllRadioCascadesDisable();
                //grabFaceRectButton.setDisable(true);
            }
        });
    }

    @FXML
    protected void defaultCascadeAction() {
        defaultCascade.setOnAction(e -> {
            setAllRadioCascadesOff();
            defaultCascade.setSelected(true);
        });
    }

    @FXML
    protected void altCascadeAction() {
        altCascade.setOnAction(e -> {
            setAllRadioCascadesOff();
            altCascade.setSelected(true);
        });
    }

    @FXML
    protected void alt2CascadeAction() {
        alt2Cascade.setOnAction(e -> {
            setAllRadioCascadesOff();
            alt2Cascade.setSelected(true);
        });
    }

    @FXML
    protected void altTreeCascadeAction() {
        altTreeCascade.setOnAction(e -> {
            setAllRadioCascadesOff();
            altTreeCascade.setSelected(true);
        });
    }

    @FXML
    protected void grabFaceRectAction() {
        grabFaceRectButton.setOnAction(e -> {
            Mat faceRect = detector.getFaceRect();
            saveGrabbedFaceRect(faceRect);
        });
    }
}
