package com.denis.detection;

import com.denis.detection.property.PropertiesReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;
import java.util.Properties;

public class Main extends Application {

    private static Stage stage;
    private Scene scene;
    private BorderPane pane;

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
        stage.setTitle("Face detection");
        stage.show();
    }

    public static void main(String[] args) {
        // load the native OpenCV library
        Properties properties = new PropertiesReader().getProperties();
        //System.setProperty("java.library.path", properties.getProperty("java.library.path"));
        //System.setProperty("java.library.path", "C:\\OpenCV\\opencv\\build\\java\\x64");
        // TODO end config.property file init, add more properties
        System.out.println(properties.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
