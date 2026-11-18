package com.imgo.cacho.model;

public class Juego {
	
	protected static final int limiteTiros = 2;
	protected static final int limiteVolteos = 2;
	public static final String[]nombresCasillas = new String[] {
		"Balas",
		"Tontos",
		"Trenes",
		"Cuadras",
		"Quinas",
		"Senas",
		"Escalera",
		"Full",
		"Poker",
		"Grande 1",
		"Grande 2"
	};
	
	protected final Jugador[] jugadores;
	protected final Cubilete cubilete;
	protected EvaluadorCacho evaCacho;
	protected int turnoActual;
	protected int tirosRestantes;
	protected int volteosRestantes;
	protected boolean esDeMano;
	
	public Juego(String[] jugadores) {
		if(jugadores == null || jugadores.length == 0) throw new IllegalArgumentException();
		this.jugadores = new Jugador[jugadores.length];
		for(int i = 0; i < jugadores.length; i++) {
			// 2. Delegamos la creación al método fábrica
			this.jugadores[i] = crearJugador(jugadores[i]); 
		}
		this.cubilete = new Cubilete();
		this.turnoActual = 0;
		prepararNuevoTurno();
	}

	// 3. Método fábrica que las clases hijas pueden sobrescribir
	protected Jugador crearJugador(String nombre) {
		return new Jugador(nombre, 1);
	}
	public void prepararNuevoTurno() {
		this.tirosRestantes = limiteTiros;
		this.volteosRestantes = limiteVolteos;
		this.esDeMano = true;
		this.cubilete.cambioMano(esDeMano);
	} 
	/*
	 * --------------------
	 * Acciones del Jugador
	 * --------------------
	 */
	public int[] realizarTiro(int[]tirados) {
		if(tirosRestantes <= 0) return cubilete.getDados();
		if(!esDeMano) {
			if(tirados.length != 0) cubilete.tirarCubilete(tirados);
			else cubilete.tirarTodos();
			cubilete.cambioMano(esDeMano);
		}
		else {
			cubilete.tirarTodos();
			esDeMano = false;
		}
		tirosRestantes--;
		return cubilete.getDados();
	}
	public boolean realizarVolteo(int volteado) {
		if(volteosRestantes <= 0 || tirosRestantes >= 2) return false;
		cubilete.voltearDados(volteado);
		volteosRestantes--;
		esDeMano = false;
		cubilete.cambioMano(esDeMano);
		return true;
	}
	public boolean anotarTurno(String nombre) {
		if(tirosRestantes >=2) return false;
		Jugador jugadorActual = jugadores[turnoActual];
		int[] valoresDados = cubilete.getDados();
		try {
			evaCacho = new EvaluadorCacho(nombre, valoresDados, cubilete.isMano());			
		} catch (IllegalArgumentException e) {
			return false;
		}
		if(!evaCacho.verificarGeneral()) return false;
		if(cubilete.isMano() && evaCacho.verificarGrande()) {
			jugadorActual.declararGanador();
			sigJugador();
			return true;
		}
		int puntosCalculados = evaCacho.calcularPuntos();
		if(puntosCalculados == 0) {
			tirosRestantes = 0;
			return tacharCasilla(nombre);
		}
		boolean exito = jugadorActual.getTablero().anotaraCasilla(nombre, puntosCalculados, evaCacho.isMano());
		if(exito) {
			System.out.println(jugadorActual.getTablero().getPuntaje(nombre));
			sigJugador();
			return true;
		}
		return false;
	}
	public boolean tacharCasilla(String nombre) {
	    // Validar que el jugador haya tirado al menos una vez en su turno
	    if (tirosRestantes > 0 && volteosRestantes > 1) return false; 
	    
	    Jugador jugadorActual = jugadores[turnoActual];
	    
	    // Tachar significa anotar 0 puntos en la casilla seleccionada
	    boolean exito = jugadorActual.getTablero().tacharCasilla(nombre);
	    
	    if (exito) {
	        sigJugador(); // Avanza al siguiente turno automáticamente
	        return true;
	    }
	    return false;
	}
	/*
	 * Protocolos internos
	 */
	protected void sigJugador() {
		turnoActual = (turnoActual + 1) % jugadores.length;
		prepararNuevoTurno();
		//Calcular ganador por tableros llenos
		if(estaJuegoTerminado()) {
			ganadorPuntos();
		}
	}
	protected boolean verificarTablerosLlenos() {
		boolean resultado = true;
		for (Jugador jugador : jugadores) {
			resultado = jugador.getTablero().estaLleno() && resultado;
			if(!resultado) break;
		}
		return resultado;
	}
	protected void ganadorPuntos() {
		int maximo = 0;
		int indiceGanador = -1;
		for (int i = 0; i < jugadores.length; i++) {
			if(jugadores[i].getTablero().calcularPuntajeTotal() > maximo) {
				indiceGanador = i;
				maximo = jugadores[i].getTablero().getPuntaje();
			}else if(jugadores[i].getTablero().calcularPuntajeTotal() == maximo) return;
		}
		jugadores[indiceGanador].declararGanador();
	}
	public Jugador getJugadorActual() {
		return jugadores[turnoActual];
	}

	public boolean estaJuegoTerminado() {
		for(Jugador jugador: jugadores) {
			if(jugador.isGanador()) return true;
		}
	    return verificarTablerosLlenos();
	}

	public String obtenerNombreGanador() {
		for (Jugador jugador : jugadores) {
			if(jugador.isGanador()) return jugador.getNombre();
		}
	    return "Empate / Sin definir";
	}
	public int[] getValores() {
		return cubilete.getDados();
	}
	public int indiceTurnoActual() {
		return turnoActual;
	}
	public int numeroJugadores() {
		return jugadores.length;
	}
	public int getPuntosJugador(int indiceJugador) {
		return jugadores[indiceJugador].getTablero().getPuntaje();
	}
	public int getPuntajeCasilla(String nombre, int index) {
		return jugadores[index].getTablero().getPuntaje(nombre);
	}
}
