package com.imgo.cacho.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JugadoresController {
	@FXML
	private ChoiceBox<Integer> eleccionNJugadores;
	@FXML
	private VBox vboxNombres;
	@FXML
	private Button btnComenzar;
	@FXML
	private ChoiceBox<String> eleccionModo; 
	
	private final List<TextField> camposNombres = new ArrayList<>();
	
	private void actualizarCampos(int cantidad) {
		vboxNombres.getChildren().clear();
		camposNombres.clear();
		
		for(int i = 1; i <= cantidad; i++) {
			TextField tf = new TextField();
			tf.setPromptText("Nombre del Jugador " + i);
			tf.getStyleClass().add("campo-texto-jugador");
			vboxNombres.getChildren().add(tf);
			camposNombres.add(tf);
		}
	}
	
	@FXML
	public void initialize() {
	    eleccionNJugadores.getItems().addAll(2, 3, 4);
	    eleccionNJugadores.setValue(2);
	    actualizarCampos(2);
	    
	    eleccionNJugadores.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
	        if(newValue != null) actualizarCampos(newValue);
	    });

	    // Inicializar las opciones del modo de juego
	    eleccionModo.getItems().addAll("Clásico", "Tripleta");
	    eleccionModo.setValue("Clásico");
	}

	@FXML
	private void onIniciarJuego(ActionEvent evento) {
	    List<String> nombresJugadores = new ArrayList<>();
	    
	    for(TextField tf : camposNombres) {
	        String nombre = tf.getText().trim();
	        if(nombre.isEmpty()) nombre = tf.getPromptText();
	        nombresJugadores.add(nombre);
	    }
	    
	    String modoSeleccionado = eleccionModo.getValue();
	    String[] nombresArray = nombresJugadores.toArray(new String[0]);
	    
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cacho.fxml"));
	        
	        CachoController controller;
	        if ("Tripleta".equals(modoSeleccionado)) {
	            controller = new CachoTripletaController(); // Clase hija
	        } else {
	            controller = new CachoController(); // Clase base
	        }
	        
	        // Le inyectamos el controlador al loader antes de cargar el FXML
	        loader.setController(controller);
	        
	        Parent root = loader.load();
	        
	        // Polimorfismo puro: ambos controladores tienen el método pasarNombres
	        controller.pasarNombres(nombresArray);
	        
	        Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
	        stage.setScene(new Scene(root));
	        //stage.setMaximized(true);
	        stage.show();
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
