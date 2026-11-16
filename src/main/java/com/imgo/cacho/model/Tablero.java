package com.imgo.cacho.model;

public class Tablero {
	private int puntaje;
	private final Casilla[] casillas = new Casilla[11];
	public Tablero() {
		puntaje = 0;
		//Ciclo for para instanciar casilla en funcion a su nombre e indice
		for(int i = 0; i < 11; i++) {
			this.casillas[i] = new Casilla(
				Juego.nombresCasillas[i],
				(i<6) ? Casilla.TipoCasilla.NUMERICA : Casilla.TipoCasilla.ESPECIAL
			);
		}
	}
	private Casilla buscarCasilla(String nombreCasilla) {
		int i = -1;
		for (int j = 0; j < 11; j++) {
			if(casillas[j].getNombre().equals(nombreCasilla)) {
				i=j;
				break;
			}
		}
		if(i==-1) throw new IllegalArgumentException(); 
		return casillas[i];
	}
	public boolean anotaraCasilla(String nombreCasilla, int puntos, boolean mano) {
		Casilla casilla = buscarCasilla(nombreCasilla);
		puntaje+=puntos; 
		return casilla.anotar(puntos, mano);
	}
	public boolean tacharCasilla(String nombreCasilla) {
		Casilla casilla = buscarCasilla(nombreCasilla);
		return casilla.tachar();
	}
	//Calcula puntaje total
	public int calcularPuntajeTotal() {
		int total = 0;
		for (Casilla casilla : casillas) {
			total+=casilla.getPuntos();
		}
		puntaje = total;
		return total;
	}
	public int getPuntaje(String nombreCasilla) {
		return buscarCasilla(nombreCasilla).getPuntos();
	}
	public int getPuntaje() {
		return puntaje;
	}
	public boolean estaLleno() {
		boolean resultado = true;
		for (Casilla casilla : casillas) {
			resultado = (casilla.getEstado() != Casilla.EstadoCasilla.VACIO) && resultado; 
			if(!resultado) break;
		}
		return resultado;
	}
}
