package com.imgo.cacho.controller;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.geometry.Pos;

import com.imgo.cacho.model.Juego;
import com.imgo.cacho.model.EvaluadorCacho;
// Importa tu modelo

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CachoController {

	@FXML
	protected ImageView imgCubilete;
	@FXML
	protected ImageView dado0, dado1, dado2, dado3, dado4;
	@FXML
	protected Pane paneMesa;
	@FXML
	protected HBox contenedorTableros;
	@FXML
	protected Label lblTurno, lblLanzamientos, lblFase;
	@FXML
	protected VBox vboxContadorPuntos;

	protected VBox[] panelesTablerosJugadores;
	protected Button[][] botonesCasillasJugadores;
	protected HBox[] contadorJugadores;
	protected Label[] lblPuntajes;

	protected ImageView[] dadosUI;
	protected boolean[] dadosSeleccionados = new boolean[5];
	protected boolean[] casillasPosibles = new boolean[11];

	// Instancia de tu Modelo Juego (puedes pasar los nombres desde la ventana de
	// menú previa)
	protected Juego juego;
	protected EvaluadorCacho evaCacho;

	// Variables de flujo de juego
	protected enum FaseTurno {
		PRIMER_TIRO, SEGUNDO_TIRO_SELECCION, TERCER_TIRO_VOLTEAR
	}

	protected FaseTurno faseActual = FaseTurno.PRIMER_TIRO;
	protected int volteosRealizadosEnTurno = 0; // Para llevar la cuenta del 1 obligatorio y 1 opcional

	// Variables del modelo
	protected boolean controlNumerico = true;
	protected int[] valoresDados = new int[5];

	/*
	 * ------------- Metodos de inicializacion -------------
	 */
	public void pasarNombres(String[] nombresJugadores) {
		dadosUI = new ImageView[] { dado0, dado1, dado2, dado3, dado4 };
		inicializarJuego(nombresJugadores);
		valoresDados = juego.getValores();
		inicializarTablerosVisuales(nombresJugadores);
		inicializarContador(nombresJugadores);
		imagenCubilete();
		actualizarUI();
	}
	protected void inicializarJuego(String[] nombresJugadores) {
		juego = new Juego(nombresJugadores);
	}
	@FXML
	public void initialize() {

	}
	@FXML
	protected void onVolverAlMenu(ActionEvent evento) {
	    try {
	        // 1. Limpiamos referencias pesadas del juego actual
	        if (juego != null) {
	            juego = null; 
	        }

	        // 2. Cargamos el menú
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
	        Parent root = loader.load();
	        
	        Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
	        
	        // 3. Forzamos a que la escena anterior pierda su raíz principal
	        stage.getScene().setRoot(new Pane()); // Limpia la escena vieja de la memoria visual
	        
	        Scene scene = new Scene(root);
	        stage.setScene(scene);
	        //stage.setMaximized(true);
	        stage.show();
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	/*
	 * ------------- Cubilete -------------
	 */
	@FXML
	protected void handleCubileteClick(MouseEvent event) {
		if (faseActual == FaseTurno.PRIMER_TIRO) {
			// Primer tiro: Lanza todos
			controlNumerico = true;
			valoresDados = juego.realizarTiro(null);
			animarDadosAlCubileteYReagitar(valoresDados);
			iluminarCasillasValidas(valoresDados);
			faseActual = FaseTurno.SEGUNDO_TIRO_SELECCION;
			lblLanzamientos.setText("Tiros restantes: 1");
			lblFase.setText("Fase: Selecciona dados a re-lanzar o click en cubilete para lanzar todos");

		} else if (faseActual == FaseTurno.SEGUNDO_TIRO_SELECCION) {
			// Segundo tiro: Recoge los dados seleccionados (o todos si no hay selección)
			controlNumerico = false;
			int[] dadosSelec = obtenerIndicesSeleccionados();
			valoresDados = juego.realizarTiro(dadosSelec);
			animarDadosAlCubileteYReagitar(valoresDados);
			iluminarCasillasValidas(valoresDados);
			// Pasamos a la fase del tercer tiro (volteos)
			faseActual = FaseTurno.TERCER_TIRO_VOLTEAR;
			volteosRealizadosEnTurno = 0;
			lblLanzamientos.setText("Tiros restantes: 0");
			lblFase.setText("Fase: Voltear dados (1 obligatorio, 1 opcional)");

		}
	}

	/*
	 * --------------------- Accion/Seleccion Dado ---------------------
	 */
	@FXML
	protected void handleDadoClick(MouseEvent event) {
		ImageView dadoClickeado = (ImageView) event.getSource();
		PauseTransition longPressTimer = new PauseTransition(Duration.millis(300)); 
		
		int index = obtenerIndiceDado(dadoClickeado);
		
	    final boolean[] esLongPress = {false};

	    // Cuando se cumple el tiempo de mantener presionado
	    longPressTimer.setOnFinished(e -> {
	        esLongPress[0] = true;
	        if(obtenerIndicesSeleccionados().length == 0) {	        	
	        	faseActual = FaseTurno.TERCER_TIRO_VOLTEAR;
	        	controlNumerico = false;
	        	ejecutarVolteoDado(index);
	        	System.out.println(volteosRealizadosEnTurno);

	        	iluminarCasillasValidas(valoresDados);
				lblLanzamientos.setText("Tiros restantes: 0");
	        }
	    });

	    // Al presionar el botón, iniciamos el conteo
	    dadoClickeado.setOnMousePressed(e -> {
	        esLongPress[0] = false;
	        longPressTimer.playFromStart();
	    });

	    // Al soltar el botón
	    dadoClickeado.setOnMouseReleased(e -> {
	        longPressTimer.stop();
	        // Si lo soltó rápido (antes de los 600ms), actúa como un clic normal (anotar)
	        if (!esLongPress[0]) {
	        	if (faseActual == FaseTurno.SEGUNDO_TIRO_SELECCION) {
	    			// Seleccionar/Deseleccionar para el segundo tiro
	    			toggleSeleccionDado(index, dadoClickeado);
	    			

	    		} else if (faseActual == FaseTurno.TERCER_TIRO_VOLTEAR) {
	    			// Fase de volteo: el usuario hace click en un dado para cambiar su valor
	    			ejecutarVolteoDado(index);
	    			System.out.println(volteosRealizadosEnTurno);
	    			iluminarCasillasValidas(valoresDados);

	    		}
	        }
	    });

	    // Si el mouse sale del botón mientras lo mantenía presionado, cancelamos
	    dadoClickeado.setOnMouseExited(e -> {
	        longPressTimer.stop();
	    });
		
	}
	protected void toggleSeleccionDado(int index, ImageView dadoUI) {
		dadosSeleccionados[index] = !dadosSeleccionados[index];
		TranslateTransition tt = new TranslateTransition(Duration.millis(150), dadoUI);
		tt.setByY(dadosSeleccionados[index] ? -20 : 20);
		tt.play();
	}
	protected void ejecutarVolteoDado(int index) {
		boolean exito = juego.realizarVolteo(index);
		valoresDados = juego.getValores();

		if (exito) {
			volteosRealizadosEnTurno++;
			actualizarImagenesDados(valoresDados);
			lblFase.setText("Fase: Voltear dados (Te queda 1 volteo opcional)");
		} else
			lblFase.setText("¡No se puede voltear ese dado en este momento!");

	}

	/*
	 * ------- Tablero -------
	 */
	protected void inicializarTablerosVisuales(String[] nombresJugadores) {
		int numJugadores = nombresJugadores.length;
		panelesTablerosJugadores = new VBox[numJugadores];
		botonesCasillasJugadores = new Button[numJugadores][11];

		contenedorTableros.getChildren().clear(); // Limpiar el HBox principal de la mesa

		for (int i = 0; i < numJugadores; i++) {
			// Contenedor principal del tablero de este jugador
			VBox tableroBox = new VBox(8);
			tableroBox.setAlignment(Pos.CENTER);
			tableroBox.setStyle(
					"-fx-background-color: #1a110e;"
					+ "-fx-padding: 10px; "
					+ "-fx-border-color: #d4af37; "
					+ "-fx-border-width: 2px;");

			// Etiqueta del nombre del jugador
			Label lblNombre = new Label(nombresJugadores[i]);
			lblNombre.setStyle(
					"-fx-text-fill: #ffd700; "
					+ "-fx-font-weight: bold; "
					+ "-fx-font-size: 14px;");
			tableroBox.getChildren().add(lblNombre);

			// 1. LA GRILLA SUPERIOR (Casillas 0 a 8: Balas, Tontos, Trenes, Cuadras,
			// Quinas, Senas, Escalera, Full, Poker)
			GridPane gridCasillas = new GridPane();
			gridCasillas.setHgap(5);
			gridCasillas.setVgap(5);
			gridCasillas.setAlignment(Pos.CENTER);

			// Llenamos la grilla
			int[]controlColumna= {0,2,1};
			int indexGrid = 0;
			for (int col = 0; col < 3; col++) {
				for (int fila = 0; fila < 3; fila++) {
					if (indexGrid < 9) { // Las primeras 9 casillas van en la grilla
						final int casillaIndex = indexGrid;
						final int jugadorIndex = i;

						Button btn = crearBotonCasilla(Juego.nombresCasillas[casillaIndex], jugadorIndex, casillaIndex);
						gridCasillas.add(btn, controlColumna[col], fila);
						indexGrid++;
					}
				}
			}
			tableroBox.getChildren().add(gridCasillas);

			// 2. EL VBOX INFERIOR (Casillas 9 y 10: Grande 1 y Grande 2)
			VBox vboxGrandes = new VBox(5);
			vboxGrandes.setAlignment(Pos.CENTER);

			for (int g = 9; g < 11; g++) {
				final int casillaIndex = g;
				final int jugadorIndex = i;

				Button btnGrande = crearBotonCasilla(Juego.nombresCasillas[casillaIndex], jugadorIndex, casillaIndex);
				vboxGrandes.getChildren().add(btnGrande);
			}

			tableroBox.getChildren().add(vboxGrandes);

			// Guardamos y agregamos al panel general de la escena
			panelesTablerosJugadores[i] = tableroBox;
			contenedorTableros.getChildren().add(tableroBox);
		}
	}
	protected Button crearBotonCasilla(String nombreCasilla, int jugadorIndex, int casillaIndex) {
		Button btn = new Button(nombreCasilla + "\n-");
		btn.setPrefWidth(85);
		btn.setPrefHeight(40);
		btn.setStyle(
				"-fx-background-color: #2b1b17;"
				+ "-fx-text-fill: white;"
				+ "-fx-border-color: #555;"
				+ "-fx-font-size: 10px;"
				+ "-fx-alignment: CENTER;");

		// Temporizador para detectar el "mantener presionado" (Long Press)
	    PauseTransition longPressTimer = new PauseTransition(Duration.millis(600)); 
	    final boolean[] esLongPress = {false};

	    // Cuando se cumple el tiempo de mantener presionado
	    longPressTimer.setOnFinished(e -> {
	        esLongPress[0] = true;
	        handleTacharCasilla(jugadorIndex, casillaIndex);
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
	            handleCasillaClick(jugadorIndex, casillaIndex);
	        }
	    });

	    // Si el mouse sale del botón mientras lo mantenía presionado, cancelamos
	    btn.setOnMouseExited(e -> {
	        longPressTimer.stop();
	    });

	    botonesCasillasJugadores[jugadorIndex][casillaIndex] = btn;
	    return btn;
	}
	protected void handleCasillaClick(int jugadorIndex, int casillaIndex) {
	    // 1. Validaciones previas
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

	    // 2. Intentar anotar en el modelo
	    boolean exito = juego.anotarTurno(nombreCasilla);
	    System.out.println("Anotación exitosa: " + exito);

	    if (exito) {
	        int puntosObtenidos = juego.getPuntajeCasilla(nombreCasilla, jugadorIndex);
	        botonesCasillasJugadores[jugadorIndex][casillaIndex].setText(nombreCasilla + ": " + puntosObtenidos);
	        botonesCasillasJugadores[jugadorIndex][casillaIndex].setDisable(true); // Bloquear casilla ya anotada

	        // 3. Comprobar si el juego terminó
	        if (juego.estaJuegoTerminado()) {
	            String ganador = juego.obtenerNombreGanador();
	            lblFase.setText("¡FIN DEL JUEGO! Ganador: " + ganador);
	        } else {
	            // El modelo ya avanzó al siguiente turno internamente al tener éxito.
	            // Reiniciamos el flujo visual para el nuevo jugador
	            faseActual = FaseTurno.PRIMER_TIRO;
	            volteosRealizadosEnTurno = 0;
	            actualizarUI();
	            lblPuntajes[jugadorIndex].setText("" + juego.getPuntosJugador(jugadorIndex));
	            lblFase.setText("Turno de: " + juego.getJugadorActual().getNombre() + " - Click en cubilete");
	        }
	    } else {
	        lblFase.setText("¡Casilla inválida o rechazada por el Evaluador!");
	    }
	}
	protected void handleTacharCasilla(int jugadorIndex, int casillaIndex) {
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

	    // 2. Llamar directamente a tacharCasilla en el Modelo
	    boolean exito = juego.tacharCasilla(nombreCasilla);
	    System.out.println("Tachado exitoso (doble clic) en " + nombreCasilla + ": " + exito);

	    if (exito) {
	        // Marcamos con 0 en la interfaz y bloqueamos el botón
	        botonesCasillasJugadores[jugadorIndex][casillaIndex].setText(nombreCasilla + ": 0");
	        botonesCasillasJugadores[jugadorIndex][casillaIndex].setDisable(true);

	        // 3. Evaluar fin de juego o pasar el turno
	        if (juego.estaJuegoTerminado()) {
	            String ganador = juego.obtenerNombreGanador();
	            lblFase.setText("¡FIN DEL JUEGO! Ganador: " + ganador);
	        } else {
	            faseActual = FaseTurno.PRIMER_TIRO;
	            volteosRealizadosEnTurno = 0;
	            actualizarUI();
	            lblFase.setText("Turno de: " + juego.getJugadorActual().getNombre() + " - Click en cubilete");
	        }
	    } else {
	        lblFase.setText("¡No se puede tachar esta casilla (ya está ocupada o no permitida)!");
	    }
	}
	protected void iluminarCasillasValidas(int[] valores) {
	    int turnoActualIdx = juego.indiceTurnoActual();

	    for (int i = 0; i < casillasPosibles.length; i++) {
	        evaCacho = new EvaluadorCacho(valores, i);
	        boolean esValida = (i >= 6 || !controlNumerico) && evaCacho.verificarGeneral();
	        casillasPosibles[i] = esValida;

	        Button btn = botonesCasillasJugadores[turnoActualIdx][i]; 
	        
	        for(int j = 0; j < juego.numeroJugadores(); j++) {	
	        	if(j != turnoActualIdx) {
	        		Button btnOtroJugador = botonesCasillasJugadores[j][i];
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
	/*
	 * -------- Contador --------
	 */
	protected void inicializarContador(String[]nombresJugadores) {
		int numJugadores = nombresJugadores.length;
		contadorJugadores = new HBox[numJugadores];
		lblPuntajes = new Label[nombresJugadores.length];
		
		final Label lblTituloMarcador = new Label("MARCADOR");
		lblTituloMarcador.setStyle(
			"-fx-text-fill: #ffd700;"
			+ "-fx-font-weight: bold; "
			+ "-fx-font-size: 14px;"
		);
		vboxContadorPuntos.getChildren().clear();
		vboxContadorPuntos.getChildren().add(lblTituloMarcador);
		for (int i = 0 ; i < nombresJugadores.length ; i++) {
			HBox contadorJugador = new HBox();
			Label lblNombre = new Label(nombresJugadores[i] + ": ");
			lblNombre.setStyle(
				"-fx-text-fill: #ffd700; "
				+ "-fx-font-weight: bold; "
				+ "-fx-font-size: 12px;"
			);
			contadorJugador.getChildren().add(lblNombre);
			int puntosJugador = juego.getPuntosJugador(i);
			Label lblPuntos = new Label(""+puntosJugador);
			lblPuntos.setStyle(
					"-fx-text-fill: #00ffcc; "
					+ "-fx-font-size: 12px;"
					);
			lblPuntajes[i] = lblPuntos;
			contadorJugador.getChildren().add(lblPuntos);
			contadorJugadores[i] = contadorJugador;
			vboxContadorPuntos.getChildren().add(contadorJugador);
		}
	}
	
	
	/*
	 * ------------ Animaciones ------------
	 */
	protected void animarAgitacionYLanzamientoTodos(int[] valores) {
		RotateTransition rt = new RotateTransition(Duration.millis(100), imgCubilete);
		rt.setByAngle(15);
		rt.setCycleCount(6);
		rt.setAutoReverse(true);
		rt.setOnFinished(e -> {
			actualizarImagenesDados(valores);
			resetearSeleccionVisual();
		});
		rt.play();
	}
	protected void animarDadosAlCubileteYReagitar(int[] valores) {
		ParallelTransition pt = new ParallelTransition();
		for (int i = 0; i < 5; i++) {
			if (dadosSeleccionados[i] || ningunDadoSeleccionado()) {
				TranslateTransition tt = new TranslateTransition(Duration.millis(250), dadosUI[i]);
				tt.setToX(imgCubilete.getLayoutX() - dadosUI[i].getLayoutX());
				tt.setToY(imgCubilete.getLayoutY() - dadosUI[i].getLayoutY());
				pt.getChildren().add(tt);
			}
		}
		pt.setOnFinished(e -> {
			// Regresar dados a su sitio y mostrar nuevos valores
			resetearPosicionesVisualesDados();
			animarAgitacionYLanzamientoTodos(valores);
		});
		pt.play();
	}

	/*
	 * ------------------ Metodos auxiliares ------------------
	 */
	protected int[] obtenerIndicesSeleccionados() {
		List<Integer> indiceSeleccionados = new ArrayList<Integer>();
		for (int i = 0; i < dadosSeleccionados.length; i++) {
			if (dadosSeleccionados[i])
				indiceSeleccionados.add(i);
		}
		return indiceSeleccionados.stream().mapToInt(Integer::intValue).toArray();
	}
	protected boolean ningunDadoSeleccionado() {
		for (boolean s : dadosSeleccionados)
			if (s)
				return false;
		return true;
	}
	protected int obtenerIndiceDado(ImageView dado) {
		for (int i = 0; i < dadosUI.length; i++) {
			if (dadosUI[i] == dado)
				return i;
		}
		return -1;
	}

	/*
	 * --------------------- Metodos graficos ---------------------
	 */
	protected void imagenCubilete() {
		try {
	        Image imgCub = new Image(
	            getClass().getResourceAsStream("/assets/cubilete/cubilete.png"),
	            240, 270, true, false
	        );
	        imgCubilete.setImage(imgCub);
	        imgCubilete.setStyle("-fx-padding: 10 20 10 20;"
	        		+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0.2, 0.2, 0, 8);");
	    } catch (Exception e) {
	        System.out.println("Error al cargar la imagen del cubilete: " + e.getMessage());
	    }
	}
	protected void actualizarImagenesDados(int[] valores) {
		System.out.println(Arrays.toString(valoresDados));
		for (int i = 0; i < dadosUI.length; i++) {
			// 'valores[i]' trae el número exacto del dado (de 1 a 6)
			int valorDado = valores[i];

			// Verificamos que la ruta construya exactamente "dado_1.png" hasta "dado_6.png"
			String rutaImagen = "/assets/dados/dado_" + valorDado + ".png";

			try {
				Image img = new Image(
						getClass().getResourceAsStream(rutaImagen),
						120, 120,
						true,
						false
						);
				dadosUI[i].setImage(img); // Mantiene el estilo pixel art nítido
				dadosUI[i].setStyle("-fx-padding: 10 20 10 20;"
		        		+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0.2, 0.2, 0, 8);");
			} catch (Exception e) {
				System.out.println(
						"Error al cargar la imagen para el valor: " + valorDado + " en la ruta: " + rutaImagen);
			}
		}
	}
	protected void resetearSeleccionVisual() {
		for (int i = 0; i < 5; i++) {
			dadosSeleccionados[i] = false;
			dadosUI[i].setTranslateY(0);
		}
	}
	protected void resetearPosicionesVisualesDados() {
		for (ImageView d : dadosUI) {
			d.setTranslateX(0);
			d.setTranslateY(0);
		}
	}
	protected void actualizarUI() {
		lblTurno.setText("Turno de: " + juego.getJugadorActual().getNombre());
		lblFase.setText("Fase: Primer Tiro (Click en cubilete)");
		actualizarImagenesDados(valoresDados);
	}
}