package com.spiga;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ui/MainView.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1400, 900);
        primaryStage.setTitle("SPIGA Simulation - Full Spec");
        primaryStage.setMaximized(true); // Start maximized
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
