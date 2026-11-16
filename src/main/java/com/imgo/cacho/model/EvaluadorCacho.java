package com.imgo.cacho.model;

public class EvaluadorCacho {
	private String nombreCasilla;
	private int[]valoresDados;
	private boolean mano;
	private int indiceCasilla;
	private int[]contador;
	
	public EvaluadorCacho(int[]valoresDados, int indiceCasilla) {
		this.valoresDados = valoresDados;
		this.indiceCasilla = indiceCasilla;
		this.contador = conteoDados();
	}
	public EvaluadorCacho(String nombreCasilla, int[]valoresDados, boolean mano) {
		if(nombreCasilla == null || valoresDados == null) throw new IllegalArgumentException();
		this.nombreCasilla = nombreCasilla;
		this.valoresDados = valoresDados;
		this.mano = mano;
		this.indiceCasilla = buscarIndiceCasilla();
		this.contador = conteoDados();
	}
	//Asocia a cada nombre de casilla a un numero
	private int buscarIndiceCasilla() {
		int i = -1;
		for (int j = 0; j < 11; j++) {
			if(nombreCasilla.equals(Juego.nombresCasillas[j])) {
				i = j;
				break;
			}
		}
		if(i==-1) throw new IllegalArgumentException();
		return i;
		
	}
	/* Hace un arreglo contador de 6 posiciones, 
	 * cada componente del arreglo es un contador para cada cara del dado.
	 *
	 * De esa manera el arreglo [1,2,0,1,0,1]
	 * Un dado marco 1 punto.
	 * Dos dados marco 2 puntos. 
	 * Un dado marco 4 puntos.
	 * Un dado marco 6 puntos.
	 * 
	 * Este método no se encarga de volteos, solo de conteo, en general toda la clase
	*/
	private int[] conteoDados() {
		int[]cont = new int[6];
		for(int i = 1; i <= cont.length; i++) {			
			for (int valDado : valoresDados) {
				if(valDado == i) cont[i-1]++;
			}
		}
		return cont;
	}
	public int calcularPuntos() {
		int resultado;
		if(indiceCasilla < contador.length) {
			resultado = (indiceCasilla + 1) * contador[indiceCasilla];
		}else if(indiceCasilla < contador.length + 3) {
			resultado = (indiceCasilla - 4) * 10 + (mano ? 5 : 0);
		}else {
			resultado = 50;
		}
		return resultado;
	}
	//Método auxiliar que cuenta cuantos ceros existen en el arreglo contador
	private boolean repeticionNCeros(int n) {
		boolean resultado = true;
		int contCeros = 0;
		int i = 0;
		while (i < contador.length && resultado) {
			if(contador[i] == 0) {
				contCeros++;
				resultado = contCeros < n + 1;
			}
			i++;
		}
		return resultado && (contCeros >= n);
	}
	/*
	 * --------------------------------
	 * Métodos de verificacion especial
	 * --------------------------------
	 */
	public boolean verificarEscalera() {
		boolean resultado = repeticionNCeros(1);
		return (contador[0] == 0 ^ contador[contador.length - 1] == 0) && resultado;
	}
	
	public boolean verificarFull() {
		boolean resultado = repeticionNCeros(contador.length - 2);
		int i = 0;
		while (i < contador.length && resultado) {
			if(contador[i] == 3 || contador[i] == 2) {
				resultado = true;
				break;
			}else if(contador[i] == 1 || contador[i] == 4) resultado = false;
			i++;
		}
		return resultado; 
	}
	public boolean verificarPoker() {
		boolean resultado = repeticionNCeros(contador.length - 2);
		int i = 0;
		while (i < contador.length && resultado) {
			if(contador[i] == 4 || contador[i] == 1) {
				resultado = true;
				break;
			}else if(contador[i] == 3 || contador[i] == 2) resultado = false;
			i++;
		}
		return resultado;
	}
	public boolean verificarGrande() {
		boolean resultado = repeticionNCeros(contador.length - 1);
		return resultado;
	}
	public boolean verificarGeneral() {
		switch (indiceCasilla) {
		case 6:
			return verificarEscalera();
		case 7:
			return verificarFull();
		case 8:
			return verificarPoker();
		default:
			if(indiceCasilla > 8) return verificarGrande();
			return true;
		}
	}
	public boolean isMano() {
		return mano;
	}
}
