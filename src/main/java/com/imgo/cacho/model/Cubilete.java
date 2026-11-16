package com.imgo.cacho.model;

public class Cubilete {
	private Dado[] dados;
	private boolean mano;
	public Cubilete(){
		dados = new Dado[5];
		for(int i = 0; i < 5; i++) {
			dados[i] = new Dado();
		}
		mano = true;
	}
	public void tirarCubilete(int[]tirados) {
		for (int i : tirados) {
			dados[i].tirarDado();
		}
	}
	public void tirarTodos() {
		int[]indiceTodos={0,1,2,3,4};//indice de todos los dados
		tirarCubilete(indiceTodos);
	}
	public int[] getDados() {
		int salida[] = new int[5];
		for (int i = 0; i < 5; i++) {
			salida[i] = dados[i].getDado();
		}
		return salida;
	}
	
	public void voltearDados(int volteados) {
		dados[volteados].voltear();
	}
	public void cambioMano(boolean mano) {
		this.mano = mano;
	}
	public boolean isMano() {
		return mano;
	}
}
