package com.imgo.cacho.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Requiere junit-jupiter (incluye junit-jupiter-params).
 *
 * Convención de nombres:
 *  - "[FALLA ESPERADA]"  -> expone un bug o falta de validación del código actual.
 *  - "[SEGÚN TUS REGLAS]" -> depende de una decisión de diseño; ajusta o borra la prueba.
 */
class EvaluadorCachoTest {

	// ------------------------------------------------------------------
	// Utilidades
	// ------------------------------------------------------------------
	private static int[] dados(String texto) {
		if (texto.isBlank()) return new int[0];
		return Arrays.stream(texto.trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
	}

	private static EvaluadorCacho ev(String casilla, boolean mano, String texto) {
		return new EvaluadorCacho(casilla, dados(texto), mano);
	}

	/** Recorre las 6^5 = 7776 manos posibles y cuenta las que cumplen el criterio. */
	private static long contarManos(Predicate<EvaluadorCacho> criterio) {
		long total = 0;
		int[] d = new int[5];
		for (int i = 0; i < 7776; i++) {
			int x = i;
			for (int k = 0; k < 5; k++) {
				d[k] = x % 6 + 1;
				x /= 6;
			}
			if (criterio.test(new EvaluadorCacho("Balas", d.clone(), false))) total++;
		}
		return total;
	}

	// ------------------------------------------------------------------
	// Validación de entradas (hoy no existe ninguna)
	// ------------------------------------------------------------------
	@Test
	@DisplayName("casilla inexistente debe lanzar IllegalArgumentException")
	void casillaInexistenteLanzaExcepcion() {
		// Hoy: indiceCasilla = -1 y calcularPuntos() revienta con ArrayIndexOutOfBounds
		assertThrows(IllegalArgumentException.class, () -> ev("Inexistente", false, "1 2 3 4 5"));
	}

	@Test
	@DisplayName("casilla null debe lanzar IllegalArgumentException")
	void casillaNullLanzaExcepcion() {
		// Hoy: NullPointerException
		assertThrows(IllegalArgumentException.class,
				() -> new EvaluadorCacho(null, new int[] { 1, 2, 3, 4, 5 }, false));
	}

	@Test
	@DisplayName("arreglo de dados null debe lanzar IllegalArgumentException")
	void dadosNullLanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> new EvaluadorCacho("Balas", null, false));
	}
	
	@Test
	@DisplayName("el evaluador no modifica el arreglo de dados recibido")
	void noModificaElArregloOriginal() {
		int[] original = { 3, 1, 4, 1, 5 };
		int[] copia = original.clone();
		EvaluadorCacho e = new EvaluadorCacho("Balas", original, false);
		e.calcularPuntos();
		e.verificarEscalera();
		e.verificarFull();
		e.verificarPoker();
		e.verificarGrande();
		assertArrayEquals(copia, original);
	}

	// ------------------------------------------------------------------
	// calcularPuntos: casillas numéricas
	// ------------------------------------------------------------------
	@ParameterizedTest(name = "{0} con [{1}] = {2}")
	@CsvSource(delimiter = '|', value = {
			"Balas   | 1 1 3 4 6 | 2",
			"Balas   | 2 3 4 5 6 | 0",
			"Tontos  | 2 2 2 5 6 | 6",
			"Trenes  | 3 3 1 2 6 | 6",
			"Cuadras | 4 4 4 4 1 | 16",
			"Quinas  | 5 1 2 3 4 | 5",
			"Senas   | 6 6 6 6 6 | 30"
	})
	void casillasNumericasSumanValorPorCantidad(String casilla, String texto, int esperado) {
		assertEquals(esperado, ev(casilla, false, texto).calcularPuntos());
		// la mano no da bonus en las casillas numéricas
		assertEquals(esperado, ev(casilla, true, texto).calcularPuntos());
	}

	@Test
	@DisplayName("el orden de los dados no altera el puntaje")
	void elOrdenNoImporta() {
		assertEquals(ev("Cuadras", false, "4 1 4 4 4").calcularPuntos(),
				ev("Cuadras", false, "4 4 4 4 1").calcularPuntos());
	}

	// ------------------------------------------------------------------
	// calcularPuntos: casillas especiales con la combinación formada
	// ------------------------------------------------------------------
	@ParameterizedTest(name = "{0} con [{1}] mano={2} -> {3}")
	@CsvSource(delimiter = '|', value = {
			"Escalera | 5 1 2 4 3 | false | 20",
			"Escalera | 2 3 4 5 6 | true  | 25",
			"Full     | 2 2 3 3 3 | false | 30",
			"Full     | 6 6 6 1 1 | true  | 35",
			"Poker    | 4 4 4 4 1 | false | 40",
			"Poker    | 1 6 6 6 6 | true  | 45",
			"Grande 1 | 3 3 3 3 3 | false | 50",
			"Grande 1 | 3 3 3 3 3 | true  | 50",
			"Grande 2 | 6 6 6 6 6 | false | 50",
			"Grande 2 | 6 6 6 6 6 | true  | 50"
	})
	void casillasEspecialesConCombinacionFormada(String casilla, String texto, boolean mano, int esperado) {
		assertEquals(esperado, ev(casilla, mano, texto).calcularPuntos());
	}


	// ------------------------------------------------------------------
	// verificarEscalera
	// ------------------------------------------------------------------
	@ParameterizedTest(name = "escalera válida [{0}]")
	@ValueSource(strings = { "1 2 3 4 5", "5 4 3 2 1", "2 3 4 5 6", "6 2 4 3 5", "3 1 5 2 4" })
	void escaleraValida(String texto) {
		assertTrue(ev("Escalera", false, texto).verificarEscalera());
	}

	@ParameterizedTest(name = "no es escalera (con repetidos) [{0}]")
	@ValueSource(strings = { "1 1 2 3 4", "3 3 3 3 3", "2 2 3 3 3", "4 4 4 4 1", "1 2 3 4 4" })
	void noEsEscaleraConRepetidos(String texto) {
		assertFalse(ev("Escalera", false, texto).verificarEscalera());
	}

	@ParameterizedTest(name = "[FALLA ESPERADA] no es escalera (5 distintos pero con hueco) [{0}]")
	@ValueSource(strings = { "1 2 3 4 6", "1 2 3 5 6", "1 2 4 5 6" })
	void noEsEscaleraConHueco(String texto) {
		// Hoy: solo se exige "exactamente un cero" en el contador y el XOR de los extremos
		// está en un if que asigna true a un valor que ya es true (código muerto).
		assertFalse(ev("Escalera", false, texto).verificarEscalera());
	}

	@Test
	@DisplayName("[FALLA ESPERADA][SEGÚN TUS REGLAS] 1-3-4-5-6 no es escalera")
	void escaleraConUnoAlFinalNoValeSiSoloSePermite1a5y2a6() {
		// El XOR de contador[0] y contador[5] indica que querías solo 1-5 y 2-6.
		// Si en tu variante también vale 3-4-5-6-1, elimina esta prueba.
		assertFalse(ev("Escalera", false, "1 3 4 5 6").verificarEscalera());
	}

	// ------------------------------------------------------------------
	// verificarFull / verificarPoker / verificarGrande
	// ------------------------------------------------------------------
	@ParameterizedTest(name = "full válido [{0}]")
	@ValueSource(strings = { "2 2 3 3 3", "3 3 3 2 2", "1 1 1 6 6", "6 5 6 5 6" })
	void fullValido(String texto) {
		assertTrue(ev("Full", false, texto).verificarFull());
	}

	@ParameterizedTest(name = "no es full [{0}]")
	@ValueSource(strings = { "4 4 4 4 1", "1 2 3 4 5", "3 3 3 3 3", "1 1 2 2 3", "1 1 1 2 3" })
	void noEsFull(String texto) {
		assertFalse(ev("Full", false, texto).verificarFull());
	}

	@ParameterizedTest(name = "poker válido [{0}]")
	@ValueSource(strings = { "4 4 4 4 1", "1 6 6 6 6", "2 3 2 2 2" })
	void pokerValido(String texto) {
		assertTrue(ev("Poker", false, texto).verificarPoker());
	}

	@ParameterizedTest(name = "no es poker [{0}]")
	@ValueSource(strings = { "2 2 3 3 3", "5 5 5 5 5", "1 1 2 3 4", "1 1 1 2 3" })
	void noEsPoker(String texto) {
		assertFalse(ev("Poker", false, texto).verificarPoker());
	}

	@ParameterizedTest(name = "grande válida [{0}]")
	@ValueSource(strings = { "1 1 1 1 1", "5 5 5 5 5", "6 6 6 6 6" })
	void grandeValida(String texto) {
		assertTrue(ev("Grande 1", false, texto).verificarGrande());
	}

	@ParameterizedTest(name = "no es grande [{0}]")
	@ValueSource(strings = { "5 5 5 5 1", "1 2 3 4 5", "2 2 3 3 3" })
	void noEsGrande(String texto) {
		assertFalse(ev("Grande 1", false, texto).verificarGrande());
	}

	// ------------------------------------------------------------------
	// Pruebas exhaustivas sobre las 7776 manos posibles
	// ------------------------------------------------------------------
	@Test
	@DisplayName("[FALLA ESPERADA] hay exactamente 240 manos de escalera (2 secuencias x 5!)")
	void totalDeEscaleras() {
		// Hoy devuelve 720: acepta cualquier mano con 5 valores distintos (6 x 5!)
		assertEquals(240, contarManos(EvaluadorCacho::verificarEscalera));
	}

	@Test
	@DisplayName("hay exactamente 300 manos de full (6 x 5 x C(5,2))")
	void totalDeFulls() {
		assertEquals(300, contarManos(EvaluadorCacho::verificarFull));
	}

	@Test
	@DisplayName("hay exactamente 150 manos de poker (6 x 5 x 5)")
	void totalDePokers() {
		assertEquals(150, contarManos(EvaluadorCacho::verificarPoker));
	}

	@Test
	@DisplayName("hay exactamente 6 manos de grande")
	void totalDeGrandes() {
		assertEquals(6, contarManos(EvaluadorCacho::verificarGrande));
	}

	@Test
	@DisplayName("ninguna mano cumple dos combinaciones especiales a la vez")
	void combinacionesSonExcluyentes() {
		long solapes = contarManos(e -> (e.verificarEscalera() ? 1 : 0) + (e.verificarFull() ? 1 : 0)
				+ (e.verificarPoker() ? 1 : 0) + (e.verificarGrande() ? 1 : 0) > 1);
		assertEquals(0, solapes);
	}

	// ------------------------------------------------------------------
	// Getter
	// ------------------------------------------------------------------
	@Test
	void isManoDevuelveLoRecibido() {
		assertTrue(ev("Balas", true, "1 2 3 4 5").isMano());
		assertFalse(ev("Balas", false, "1 2 3 4 5").isMano());
	}
}
