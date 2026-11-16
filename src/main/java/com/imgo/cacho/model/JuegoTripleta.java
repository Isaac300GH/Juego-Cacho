package com.imgo.cacho.model;

public class JuegoTripleta extends Juego{
	public JuegoTripleta(String[]jugadores) {
		super(jugadores);
	}
	@Override
	protected Jugador crearJugador(String nombre) {
		return new Jugador(nombre, 3);
	}
	
	public boolean anotarTurno(String nombre, int numeroTablero) {
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
		int puntosCalculados =  evaCacho.calcularPuntos();
		if(puntosCalculados == 0) return tacharCasilla(nombre, numeroTablero);
		puntosCalculados*= numeroTablero + 1 ;
		boolean exito = jugadorActual.getTableroIndice(numeroTablero).anotaraCasilla(nombre, puntosCalculados, evaCacho.isMano());
		if(exito) {
			System.out.println(jugadorActual.getTableroIndice(numeroTablero).getPuntaje(nombre));
			sigJugador();
			return true;
		}
		return false;
	}
	public boolean tacharCasilla(String nombre, int numeroTablero) {
	    // Validar que el jugador haya tirado al menos una vez en su turno
	    if (tirosRestantes > 0 && volteosRestantes > 1) return false; 
	    
	    Jugador jugadorActual = jugadores[turnoActual];
	    
	    // Tachar significa anotar 0 puntos en la casilla seleccionada
	    boolean exito = jugadorActual.getTableroIndice(numeroTablero).tacharCasilla(nombre);
	    
	    if (exito) {
	        sigJugador(); // Avanza al siguiente turno automáticamente
	        return true;
	    }
	    return false;
	}
	@Override
	protected boolean verificarTablerosLlenos() {
		boolean resultado = true;
		for (Jugador jugador : jugadores) {
			for(int i = 0; i < 3; i++) {				
				resultado = jugador.getTableroIndice(i).estaLleno() && resultado;
				if(!resultado) break;
			}
		}
		return resultado;
	}
	@Override
	public int getPuntosJugador(int indiceJugador) {
		int suma = 0;
		for(int j = 0; j < 3; j++) {				
			suma += jugadores[indiceJugador].getTableroIndice(j).getPuntaje();
		}
		return suma;
	}
	@Override
	protected void ganadorPuntos() {
		int maximo = 0;
		int indiceGanador = -1;
		for (int i = 0; i < jugadores.length; i++) {
			if(getPuntosJugador(i) > maximo) {
				indiceGanador = i;
				maximo = getPuntosJugador(i);
			}else if(getPuntosJugador(i) == maximo) return;
		}
		jugadores[indiceGanador].declararGanador();
	}
	public int getPuntajeCasilla(String nombre, int index, int numeroTablero) {
		return jugadores[index].getTableroIndice(numeroTablero).getPuntaje(nombre);
	}
}
