package com.denis.detection.controllers;

import com.denis.detection.cascadeDetecting.CascadePaths;
import com.denis.detection.cascadeDetecting.FaceDetector;
import com.denis.detection.image.Filters;
import com.denis.detection.image.Utils;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CamWindowController {

    // cam javafx items
    @FXML
    private Button startCam;
    @FXML
    private ImageView camView;

    // filter javafx items
    @FXML
    private MenuItem bgr2hlsItem;
    @FXML
    private MenuItem bgr2hsvItem;
    @FXML
    private MenuItem bgr2grayItem;
    @FXML
    private MenuItem bgr2bgr555Item;
    @FXML
    private MenuItem bgr2xyzItem;
    @FXML
    private MenuItem bgr2bgr565Item;
    @FXML
    private MenuItem bgr2hlsfullItem;
    @FXML
    private MenuItem bgr2hsvfullItem;
    @FXML
    private MenuItem bgr2labItem;
    @FXML
    private MenuItem bgr2luvItem;
    @FXML
    private MenuItem bgr2rgbItem;
    @FXML
    private MenuItem bgr2ycrcbItem;
    @FXML
    private MenuItem bgr2yuvItem;
    @FXML
    private MenuItem bgr2yuvi420Item;
    @FXML
    private MenuItem bgr2yuviyuvItem;
    @FXML
    private MenuItem bgr2yuvyv12Item;

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

    // logo javafx items
    @FXML
    private CheckBox addLogo;
    private Mat logo;

    @FXML
    private TextArea loggingArea;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;

    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();

    // a flag to change the button behavior
    private boolean cameraActive = false;

    // the id of the camera to be used
    private static int cameraId = 0;

    // now uses filter
    private static int filter = Filters.FILTER_BGR2RGB;

    boolean firstInto = false;

    @FXML
    protected void startCam(ActionEvent event) {
        firstInto = true;
        defaultCascade.setSelected(true);
        setAllRadioCascadesDisable();
        detectFaceCheckBox.setSelected(false);
        if (!this.cameraActive) {
            log("Start button have been touched");

            // start the video capture
            this.capture.open(cameraId);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                log("Starting cam thread...");
                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        // effectively grab and process a single frame
                        Mat frame = grabFrame();

                        // convert and show the frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(camView, imageToShow);

                        //update(frame);
                    }
                };

                // init timer
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 15, TimeUnit.MILLISECONDS);

                // update the button content
                this.startCam.setText("Stop Camera");
            } else {
                logError("Impossible to open the camera connection...");
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
                    //Imgproc.blur(frame, frame, new Size(15, 7));
                    //Imgproc.GaussianBlur(frame, frame, new Size(9, 9), 3);
                    Imgproc.pyrDown(frame, frame);
                    Imgproc.pyrDown(frame, frame);
                    loadFaceRect(frame);
                    setFilter(frame, frame);
                }

            } catch (Exception e) {
                logError("Exception during the image elaboration: " + e);
            }
        }
        return frame;
    }

    private void loadFaceRect(Mat image) {
        FaceDetector detector = new FaceDetector();
        if (detectFaceCheckBox.isSelected()) {
            if (!defaultCascade.isSelected()) {
                detector.loadCascade(CascadePaths.faceDefaultCascadePath);
                detector.detectAndDrawFace(image);
            } else if (!altCascade.isSelected()) {
                detector.loadCascade(CascadePaths.faceAltCascadePath);
                detector.detectAndDrawFace(image);
            } else if (!alt2Cascade.isSelected()) {
                detector.loadCascade(CascadePaths.faceAlt2CascadePath);
                detector.detectAndDrawFace(image);
            } else if (!altTreeCascade.isSelected()) {
                detector.loadCascade(CascadePaths.faceAltTreeCascadePath);
                detector.detectAndDrawFace(image);
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
                logError("Exception in stopping the frame capture, trying to release the camera now... " + e);
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

    private void setFilter(Mat src, Mat dst) {
        Imgproc.cvtColor(src, dst, filter);
    }

    private void ennableAllFiltersItems() {
        bgr2hlsItem.setDisable(false);
        bgr2hsvItem.setDisable(false);
        bgr2grayItem.setDisable(false);
        bgr2bgr555Item.setDisable(false);
        bgr2xyzItem.setDisable(false);
        bgr2bgr565Item.setDisable(false);
        bgr2hlsfullItem.setDisable(false);
        bgr2hsvfullItem.setDisable(false);
        bgr2labItem.setDisable(false);
        bgr2luvItem.setDisable(false);
        bgr2rgbItem.setDisable(false);
        bgr2ycrcbItem.setDisable(false);
        bgr2yuvItem.setDisable(false);
        bgr2yuvi420Item.setDisable(false);
        bgr2yuviyuvItem.setDisable(false);
        bgr2yuvyv12Item.setDisable(false);
    }

    private Mat getFaceDetectedRect(Mat image) {
        Mat faceMat = new Mat();

        return null;
    }

    private void saveImage(Image image, String src) {
        try {
            String format = ".png";
            File file = new File(src);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), format, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    // TODO delete this damn logging system
    protected void logError(String logMessage) {
        System.err.println(logMessage);
        loggingArea.setText(loggingArea.getText() + "Error: " + logMessage + "\n");
    }

    protected void log(String logMessage) {
        System.out.println(logMessage);
        loggingArea.setText(loggingArea.getText() + logMessage + "\n");
    }

    protected void buttonTouchLog(String buttonName) {
        log(buttonName + " button have been touched");
    }

    protected void setClosed() {
        this.stopAcquisition();
    }

    @FXML
    protected void detectFaceAction(ActionEvent event) {
        detectFaceCheckBox.setOnAction(e -> {
            if (detectFaceCheckBox.isSelected())
                setAllRadioCascadesEnable();
            else setAllRadioCascadesDisable();
        });
    }

    @FXML
    protected void defaultCascadeAction(ActionEvent event) {
        defaultCascade.setOnAction(e -> {
            setAllRadioCascadesOff();
            defaultCascade.setSelected(true);
        });
    }

    @FXML
    protected void altCascadeAction(ActionEvent event) {
        altCascade.setOnAction(e -> {
            setAllRadioCascadesOff();
            altCascade.setSelected(true);
        });
    }

    @FXML
    protected void alt2CascadeAction(ActionEvent event) {
        alt2Cascade.setOnAction(e -> {
            setAllRadioCascadesOff();
            alt2Cascade.setSelected(true);
        });
    }

    @FXML
    protected void altTreeCascadeAction(ActionEvent event) {
        altTreeCascade.setOnAction(e -> {
            setAllRadioCascadesOff();
            altTreeCascade.setSelected(true);
        });
    }

    @FXML
    protected void bgr2hlsAction(ActionEvent event) {
        bgr2hlsItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2hlsItem.setDisable(true);
            filter = Filters.FILTER_BGR2HLS;
            buttonTouchLog("FILTER_BGR2HLS");
        });
    }

    @FXML
    protected void bgr2hsvAction(ActionEvent event) {
        bgr2hsvItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2hsvItem.setDisable(true);
            filter = Filters.FILTER_BGR2HSV;
            buttonTouchLog("FILTER_BGR2HSV");
        });
    }

    @FXML
    protected void bgr2greyAction(ActionEvent event) {
        bgr2grayItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2grayItem.setDisable(true);
            filter = Filters.FILTER_BGR2GRAY;
            buttonTouchLog("FILTER_BGR2GRAY");
        });
    }

    @FXML
    protected void bgr2bgr555Action(ActionEvent event) {
        bgr2bgr555Item.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2bgr555Item.setDisable(true);
            filter = Filters.FILTER_BGR2BGR555;
            buttonTouchLog("FILTER_BGR2BGR555");
        });
    }

    @FXML
    protected void bgr2xyzAction(ActionEvent event) {
        bgr2xyzItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2xyzItem.setDisable(true);
            filter = Filters.FILTER_BGR2XYZ;
            buttonTouchLog("FILTER_BGR2XYZ");
        });
    }

    @FXML
    protected void bgr2bgr565Action(ActionEvent event) {
        bgr2bgr565Item.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2bgr565Item.setDisable(true);
            filter = Filters.FILTER_BGR2BGR565;
            buttonTouchLog("FILTER_BGR2BGR565");
        });
    }

    @FXML
    protected void bgr2hlsfullAction(ActionEvent event) {
        bgr2hlsfullItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2hlsfullItem.setDisable(true);
            filter = Filters.FILTER_BGR2HLS_FULL;
            buttonTouchLog("FILTER_BGR2HLS_FULL");
        });
    }

    @FXML
    protected void bgr2hsvfullAction(ActionEvent event) {
        bgr2hsvfullItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2hsvfullItem.setDisable(true);
            filter = Filters.FILTER_BGR2HSV_FULL;
            buttonTouchLog("FILTER_BGR2HSV_FULL");
        });
    }

    @FXML
    protected void bgr2labAction(ActionEvent event) {
        bgr2labItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2labItem.setDisable(true);
            filter = Filters.FILTER_BGR2Lab;
            buttonTouchLog("FILTER_BGR2Lab");
        });
    }

    @FXML
    protected void bgr2luvAction(ActionEvent event) {
        bgr2luvItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2luvItem.setDisable(true);
            filter = Filters.FILTER_BGR2Luv;
            buttonTouchLog("FILTER_BGR2Luv");
        });
    }

    @FXML
    protected void bgr2rgbAction(ActionEvent event) {
        bgr2rgbItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2rgbItem.setDisable(true);
            filter = Filters.FILTER_BGR2RGB;
            buttonTouchLog("FILTER_BGR2RGB");
        });
    }

    @FXML
    protected void bgr2ycrcbAction(ActionEvent event) {
        bgr2ycrcbItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2ycrcbItem.setDisable(true);
            filter = Filters.FILTER_BGR2YCrCb;
            buttonTouchLog("FILTER_BGR2YCrCb");
        });
    }

    @FXML
    protected void bgr2yuvAction(ActionEvent event) {
        bgr2yuvItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2yuvItem.setDisable(true);
            filter = Filters.FILTER_BGR2YUV;
            buttonTouchLog("FILTER_BGR2YUV");
        });
    }

    @FXML
    protected void bgr2yuvi420Action(ActionEvent event) {
        bgr2yuvi420Item.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2yuvi420Item.setDisable(true);
            filter = Filters.FILTER_BGR2YUV_I420;
            buttonTouchLog("FILTER_BGR2YUV_I420");
        });
    }

    @FXML
    protected void bgr2yuviyuvAction(ActionEvent event) {
        bgr2yuviyuvItem.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2yuviyuvItem.setDisable(true);
            filter = Filters.FILTER_BGR2YUV_IYUV;
            buttonTouchLog("FILTER_BGR2YUV_IYUV");
        });
    }

    @FXML
    protected void bgr2yuvyv12Action(ActionEvent event) {
        bgr2yuvyv12Item.setOnAction(e -> {
            ennableAllFiltersItems();
            bgr2yuvyv12Item.setDisable(true);
            filter = Filters.FILTER_BGR2YUV_YV12;
            buttonTouchLog("FILTER_BGR2YUV_YV12");
        });
    }
}
