package com.imgo.cacho.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MenuController {

	@FXML
	void onNuevaPartida(ActionEvent evento) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/jugadores.fxml"));
			Parent root = loader.load();
			
			Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	void onSalir(ActionEvent evento) {
		Platform.exit();
	}
}
