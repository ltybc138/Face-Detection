package com.denis.detection;

import com.denis.detection.logging.Log;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;

public class Main extends Application {

    public static Stage stage;
    private Scene scene;
    public BorderPane pane;

    public static Log logger;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        try {
            pane = FXMLLoader.load(Main.class.getResource("CamWindow.fxml"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // load the native OpenCV library
        // TODO try to set VM options in program
        logger = new Log();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
        logger.closeLogger();
    }
}
