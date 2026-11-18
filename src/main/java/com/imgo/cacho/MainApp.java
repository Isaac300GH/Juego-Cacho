package com.imgo.cacho;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
        	
			FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
			Scene scene = new Scene(fxmlloader.load(), 1080, 720);
			primaryStage.setTitle("Cachito");
			primaryStage.setScene(scene);

			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) {
        launch(args);
    }
}