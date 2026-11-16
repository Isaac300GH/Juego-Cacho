package com.imgo.cacho.model;

public class Casilla {
	public enum TipoCasilla{NUMERICA, ESPECIAL}
	public enum EstadoCasilla{VACIO, TACHADO, LLENO}
	
	private final String nombre;
	private final TipoCasilla tipo;
	private int puntos;
	private EstadoCasilla estado;
	private boolean mano;
	
	public Casilla(String nombre, TipoCasilla tipo) {
		if(tipo == null || nombre == null) throw new IllegalArgumentException(); 
		this.nombre = nombre;
		this.tipo = tipo;
		this.puntos = 0;
		this.estado = EstadoCasilla.VACIO;
		this.mano = false;
	}
	
	public boolean anotar(int puntos, boolean mano){
		if(estado != EstadoCasilla.VACIO) return false;
		this.puntos = (puntos < 0 ? 0 : puntos); 
		this.mano = mano;
		estado = EstadoCasilla.LLENO;
		return true;
	}
	
	public boolean tachar() {
		if(estado != EstadoCasilla.VACIO) return false;
		this.puntos = 0;
		this.mano = false;
		estado = EstadoCasilla.TACHADO;
		return true;
	}
	public void reiniciar() {
		this.estado = EstadoCasilla.VACIO;
		this.puntos = 0;
		this.mano = false;
	}
	public boolean esCasillaEspecial() {
		return this.tipo == TipoCasilla.ESPECIAL;
	}public String getNombre() {
		return nombre;
	}public int getPuntos() {
		return puntos;
	}public EstadoCasilla getEstado() {
		return estado;
	}public boolean esMano() {
		return this.mano;
	}
}
