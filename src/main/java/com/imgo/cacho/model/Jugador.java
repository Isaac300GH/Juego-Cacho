package com.imgo.cacho.model;

public class Jugador {
	private String nombre;
	private Tablero[] tableros;
	private boolean ganador;
	public Jugador(String nombre, int numeroTableros) {
		if(nombre == null || nombre.isBlank() || numeroTableros <= 0) throw new IllegalArgumentException(); 
		this.nombre = nombre;
		this.ganador = false;
		tableros = new Tablero[numeroTableros];
		for(int i = 0 ; i < numeroTableros; i++){			
			this.tableros[i] = new Tablero();
		}
	}
	public String getNombre() {
		return nombre;
	}
	public Tablero getTableroIndice(int indice) {
		return tableros[indice];
	}
	public Tablero getTablero() {
		return getTableroIndice(0);
	}
	public Tablero[] getTableros() {
		return tableros;
	}
	public void declararGanador() {
		this.ganador = true;
	}
	public boolean isGanador() {
		return ganador;
	}
	
}
