package me.zacwood.attics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("BrowseView.fxml"));
            primaryStage.setTitle("attics");
            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.show();
        }
        catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void stop() {
        try {
            FileUtils.deleteDirectory(new File("items"));
        } catch (IOException e) {
            System.err.println(e.toString());
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
