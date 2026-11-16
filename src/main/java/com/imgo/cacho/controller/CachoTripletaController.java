package com.imgo.cacho.controller;

import com.imgo.cacho.model.EvaluadorCacho;
import com.imgo.cacho.model.Juego;
import com.imgo.cacho.model.JuegoTripleta;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class CachoTripletaController extends CachoController {

    // Estructura para guardar los botones organizados por [jugador][tablero 0-2][casilla 0-10]
    protected Button[][][] botonesTripletaJugadores;
    // Para saber qué pestaña (tablero 0, 1 o 2) está viendo cada jugador actualmente
    protected TabPane[] tabPanesJugadores;

    @Override
    protected void inicializarJuego(String[] nombresJugadores) {
        juego = new JuegoTripleta(nombresJugadores);
    }

    @Override
    protected void inicializarTablerosVisuales(String[] nombresJugadores) {
        int numJugadores = nombresJugadores.length;
        panelesTablerosJugadores = new VBox[numJugadores];
        tabPanesJugadores = new TabPane[numJugadores];
        botonesTripletaJugadores = new Button[numJugadores][3][11];

        contenedorTableros.getChildren().clear();

        for (int i = 0; i < numJugadores; i++) {
            VBox jugadorBox = new VBox(5);
            jugadorBox.setAlignment(Pos.CENTER);
            jugadorBox.setStyle(
                "-fx-background-color: #1a110e;"
                + "-fx-padding: 8px; "
                + "-fx-border-color: #d4af37; "
                + "-fx-border-width: 2px;"
            );

            Label lblNombre = new Label(nombresJugadores[i] + " (Tripleta)");
            lblNombre.setStyle(
            		"-fx-text-fill: #ffd700; "
            		+ "-fx-font-weight: bold; "
            		+ "-fx-font-size: 13px;");
            jugadorBox.getChildren().add(lblNombre);

            // Creamos un TabPane para alternar entre los 3 tableros (x1, x2, x3)
            TabPane tabPane = new TabPane();
            tabPane.setPrefWidth(280);
            tabPane.setPrefHeight(250);
            tabPanesJugadores[i] = tabPane;

            for (int t = 0; t < 3; t++) {
                Tab tab = new Tab("x" + (t + 1));
                tab.setClosable(false);

                VBox tableroUnicoBox = new VBox(5);
                tableroUnicoBox.setAlignment(Pos.CENTER);

                // Grilla de casillas 0 a 8
                GridPane gridCasillas = new GridPane();
                gridCasillas.setHgap(3);
                gridCasillas.setVgap(3);
                gridCasillas.setAlignment(Pos.CENTER);

                int[] controlColumna = {0, 2, 1};
                int indexGrid = 0;
                for (int col = 0; col < 3; col++) {
                    for (int fila = 0; fila < 3; fila++) {
                        if (indexGrid < 9) {
                            final int casillaIndex = indexGrid;
                            final int jugadorIndex = i;
                            final int tableroIndex = t;

                            Button btn = crearBotonCasillaTripleta(Juego.nombresCasillas[casillaIndex], jugadorIndex, tableroIndex, casillaIndex);
                            gridCasillas.add(btn, controlColumna[col], fila);
                            indexGrid++;
                        }
                    }
                }
                tableroUnicoBox.getChildren().add(gridCasillas);

                // Casillas 9 y 10 (Grandes)
                VBox vboxGrandes = new VBox(3);
                vboxGrandes.setAlignment(Pos.CENTER);
                for (int g = 9; g < 11; g++) {
                    final int casillaIndex = g;
                    final int jugadorIndex = i;
                    final int tableroIndex = t;

                    Button btnGrande = crearBotonCasillaTripleta(Juego.nombresCasillas[casillaIndex], jugadorIndex, tableroIndex, casillaIndex);
                    vboxGrandes.getChildren().add(btnGrande);
                }
                tableroUnicoBox.getChildren().add(vboxGrandes);

                tab.setContent(tableroUnicoBox);
                tabPane.getTabs().add(tab);
            }

            jugadorBox.getChildren().add(tabPane);
            panelesTablerosJugadores[i] = jugadorBox;
            contenedorTableros.getChildren().add(jugadorBox);
        }
    }

    protected Button crearBotonCasillaTripleta(String nombreCasilla, int jugadorIndex, int tableroIndex, int casillaIndex) {
        Button btn = new Button(nombreCasilla + "\n-");
        btn.setPrefWidth(75);
        btn.setPrefHeight(32);
        btn.setStyle(
            "-fx-background-color: #2b1b17;"
            + "-fx-text-fill: white;"
            + "-fx-border-color: #555;"
            + "-fx-font-size: 9px;"
            + "-fx-alignment: CENTER;"
        );

        PauseTransition longPressTimer = new PauseTransition(Duration.millis(600)); 
	    final boolean[] esLongPress = {false};

	    // Cuando se cumple el tiempo de mantener presionado
	    longPressTimer.setOnFinished(e -> {
	        esLongPress[0] = true;
	        handleTacharCasillaTripleta(jugadorIndex, tableroIndex, casillaIndex);
	    });

	    // Al presionar el botón, iniciamos el conteo
	    btn.setOnMousePressed(e -> {
	        esLongPress[0] = false;
	        longPressTimer.playFromStart();
	    });

	    // Al soltar el botón
	    btn.setOnMouseReleased(e -> {
	        longPressTimer.stop();
	        // Si lo soltó rápido (antes de los 600ms), actúa como un clic normal (anotar)
	        if (!esLongPress[0]) {
	        	handleCasillaClickTripleta(jugadorIndex, tableroIndex, casillaIndex);
	        }
	    });

	    // Si el mouse sale del botón mientras lo mantenía presionado, cancelamos
	    btn.setOnMouseExited(e -> {
	        longPressTimer.stop();
	    });
        
        botonesTripletaJugadores[jugadorIndex][tableroIndex][casillaIndex] = btn;
        return btn;
    }

    protected void handleCasillaClickTripleta(int jugadorIndex, int tableroIndex, int casillaIndex) {
    	if ((faseActual == FaseTurno.PRIMER_TIRO || volteosRealizadosEnTurno < 1) && faseActual != FaseTurno.SEGUNDO_TIRO_SELECCION) {
	        lblFase.setText("¡Primero debes lanzar/voltear los dados!");
	        return;
	    }
	    // Validar que sea el turno del jugador correspondiente
	    if (jugadorIndex != juego.indiceTurnoActual()) {
	        lblFase.setText("¡No es el turno de este jugador!");
	        return;
	    }

	    // Validar que la casilla esté permitida para los dados actuales
	    if (!casillasPosibles[casillaIndex]) {
	        lblFase.setText("¡Esta casilla no es válida para estos dados!");
	        return;
	    }

        String nombreCasilla = Juego.nombresCasillas[casillaIndex];
        JuegoTripleta juegoT = (JuegoTripleta) juego;

        boolean exito = juegoT.anotarTurno(nombreCasilla, tableroIndex);

        if (exito) {
            int puntosObtenidos = juegoT.getPuntajeCasilla(nombreCasilla, jugadorIndex, tableroIndex);
            botonesTripletaJugadores[jugadorIndex][tableroIndex][casillaIndex].setText(nombreCasilla + ": " + puntosObtenidos);
            botonesTripletaJugadores[jugadorIndex][tableroIndex][casillaIndex].setDisable(true);

            if (juegoT.estaJuegoTerminado()) {
                lblFase.setText("¡FIN DEL JUEGO! Ganador: " + juegoT.obtenerNombreGanador());
            } else {
                faseActual = FaseTurno.PRIMER_TIRO;
                volteosRealizadosEnTurno = 0;
                actualizarUI();
	            lblPuntajes[jugadorIndex].setText("" + juego.getPuntosJugador(jugadorIndex));
                lblFase.setText("Turno de: " + juegoT.getJugadorActual().getNombre() + " - Click en cubilete");
            }
        } else {
            lblFase.setText("¡Casilla inválida o no permitida en este tablero!");
        }
    }
    protected void handleTacharCasillaTripleta(int jugadorIndex, int tableroIndex ,int casillaIndex) {
	    // 1. Validaciones previas
	    if (faseActual == FaseTurno.PRIMER_TIRO && volteosRealizadosEnTurno == 0) {
	        lblFase.setText("¡Primero debes lanzar/voltear los dados para tachar!");
	        return;
	    }
	    
	    if (jugadorIndex != juego.indiceTurnoActual()) {
	        lblFase.setText("¡No es el turno de este jugador!");
	        return;
	    }

	    String nombreCasilla = Juego.nombresCasillas[casillaIndex];
	    JuegoTripleta juegoT = (JuegoTripleta) juego;
	    
	    // 2. Llamar directamente a tacharCasilla en el Modelo
	    boolean exito = juegoT.tacharCasilla(nombreCasilla, tableroIndex);
	    System.out.println("Tachado exitoso (doble clic) en " + nombreCasilla + ": " + exito);

	    if (exito) {
	        // Marcamos con 0 en la interfaz y bloqueamos el botón
            botonesTripletaJugadores[jugadorIndex][tableroIndex][casillaIndex].setText(nombreCasilla + ": 0");
            botonesTripletaJugadores[jugadorIndex][tableroIndex][casillaIndex].setDisable(true);

	        // 3. Evaluar fin de juego o pasar el turno
	        if (juegoT.estaJuegoTerminado()) {
	            String ganador = juegoT.obtenerNombreGanador();
	            lblFase.setText("¡FIN DEL JUEGO! Ganador: " + ganador);
	        } else {
	            faseActual = FaseTurno.PRIMER_TIRO;
	            volteosRealizadosEnTurno = 0;
	            actualizarUI();
	            lblFase.setText("Turno de: " + juegoT.getJugadorActual().getNombre() + " - Click en cubilete");
	        }
	    } else {
	        lblFase.setText("¡No se puede tachar esta casilla (ya está ocupada o no permitida)!");
	    }
	}
    @Override
    protected void iluminarCasillasValidas(int[] valores) {
	    int turnoActualIdx = juego.indiceTurnoActual();
	    for(int j = 0 ; j < 3; j++) {
	    	for (int i = 0; i < casillasPosibles.length; i++) {
	    		evaCacho = new EvaluadorCacho(valores, i);
	    		boolean esValida = (i >= 6 || !controlNumerico) && evaCacho.verificarGeneral();
	    		casillasPosibles[i] = esValida;
	    		
	    		Button btn = botonesTripletaJugadores[turnoActualIdx][j][i]; 
	    		
	    		for(int k = 0; k < juego.numeroJugadores(); k++) {	
		        	if(k != turnoActualIdx) {
		        		Button btnOtroJugador = botonesTripletaJugadores[k][j][i];
			            btnOtroJugador.setDisable(true);
		        	}
		        }
	    		
	    		// Si la casilla ya fue anotada antes por este jugador, la mantenemos deshabilitada
	    		if (btn.getText().contains(":")) {
	    			btn.setDisable(true);
	    			continue;
	    		}
	    		
	    		if (esValida) {
	    			// Iluminar en verde brillante si es válida para anotar en este momento
	    			btn.setStyle(
	    					"-fx-background-color: #2e5a27;"
	    							+ "-fx-text-fill: white;"
	    							+ "-fx-border-color: #00ff00;"
	    							+ "-fx-border-width: 2px;"
	    					);
	    			btn.setDisable(false); // ¡Permite hacer click para anotar de inmediato!
	    		} else {
	    			// Apagar o normalizar si no cumple con los dados actuales
	    			btn.setStyle("-fx-background-color: #2b1b17;"
	    					+ "-fx-text-fill: #888;"
	    					+ "-fx-border-color: #444;");
	    			btn.setDisable(false); // Evita clicks en casillas no válidas
	    		}
	    	}
	    }
	}
}
