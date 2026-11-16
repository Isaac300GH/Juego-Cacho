package com.imgo.cacho.model;
import java.security.SecureRandom;
public class Dado {
	private SecureRandom rd;
	private int valor;
	public Dado() {
		rd = new SecureRandom();
		valor = 1;
	}
	public void tirarDado() {
		valor = 1 + rd.nextInt(6);
	}
	public int getDado() {
		return valor;
	}
	public void voltear() {
		valor = 7 - valor;
	} 
}
