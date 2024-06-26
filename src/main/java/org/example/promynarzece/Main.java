package org.example.promynarzece;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("MainMenuScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), Color.LIGHTSKYBLUE);
        stage.setResizable(false);

        // Ustalenie tytułu i logo projektu
        stage.setTitle("Promy na rzece");
        Image icon = new Image("Logo_WAT.jpg");
        stage.getIcons().add(icon);

        // ustawienie sceny
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}