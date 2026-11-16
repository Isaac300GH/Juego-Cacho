package com.imgo.cacho.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de Juego contra su API pública actual.
 *
 * Como Juego no expone sus contadores ni permite inyectar los dados, hay dos soluciones temporales:
 *  1) Se leen campos privados con reflexión (ver entero() y jugadores()).
 *  2) Los dados son aleatorios, así que las pruebas solo afirman cosas que no dependen del azar,
 *     y cuando necesitan una tirada concreta repiten partidas hasta obtenerla.
 * Si inyectas el Cubilete en el constructor (ver recomendaciones) todo esto se simplifica.
 *
 * "[FALLA ESPERADA]" = expone un bug o una regla faltante en el código actual.
 */
class JuegoTest {

	/**
	 * Argumento para relanzar dados. 1 y 2 son válidos tanto si tu Cubilete los interpreta como
	 * índices (0-4) como si los interpreta como valores (1-6). Ajusta si usas otro formato.
	 */
	private static final int[] RELANZAR = { 1, 2 };
	private static final int VOLTEAR = 1;
	private static final String[] NOMBRES = { "Ana", "Beto" };

	private Juego juego;

	@BeforeEach
	void iniciar() {
		juego = new Juego(NOMBRES.clone());
	}

	// ------------------------------------------------------------------
	// Utilidades
	// ------------------------------------------------------------------
	private static int entero(Juego j, String campo) throws ReflectiveOperationException {
		Field f = Juego.class.getDeclaredField(campo);
		f.setAccessible(true);
		return f.getInt(j);
	}

	private static Jugador[] jugadores(Juego j) throws ReflectiveOperationException {
		Field f = Juego.class.getDeclaredField("jugadores");
		f.setAccessible(true);
		return (Jugador[]) f.get(j);
	}

	/** Dos tiros: el segundo quita la condición de "mano", así no hay Grande de mano aleatoria. */
	private static void tirarSinMano(Juego j) {
		j.realizarTiro(new int[0]);
		j.realizarTiro(RELANZAR);
	}


	/** 5 valores distintos que incluyen 1 y 6: no es escalera, full, poker ni grande. */
	private static boolean noFormaNingunaCombinacion(int[] dados) {
		boolean[] vistos = new boolean[7];
		int distintos = 0;
		for (int d : dados) {
			if (!vistos[d]) {
				vistos[d] = true;
				distintos++;
			}
		}
		return distintos == 5 && vistos[1] && vistos[6];
	}

	private static Juego juegoSinCombinaciones() {
		for (int i = 0; i < 100_000; i++) {
			Juego j = new Juego(NOMBRES.clone());
			j.realizarTiro(new int[0]);
			int[] dados = j.realizarTiro(RELANZAR).clone();
			if (noFormaNingunaCombinacion(dados)) return j;
		}
		throw new AssertionError("No se obtuvo una tirada sin combinaciones");
	}

	// ------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------
	@Test
	@DisplayName("empieza jugando el primer jugador")
	void empiezaElPrimerJugador() throws Exception {
		assertNotNull(juego.getJugadorActual());
		assertSame(jugadores(juego)[0], juego.getJugadorActual());
	}

	@Test
	@DisplayName("[FALLA ESPERADA] sin jugadores debe lanzar IllegalArgumentException")
	void sinJugadoresLanzaExcepcion() {
		// Hoy: el constructor funciona y el error aparece más tarde (% 0 o AIOOBE)
		assertThrows(IllegalArgumentException.class, () -> new Juego(new String[0]));
	}

	@Test
	@DisplayName("[FALLA ESPERADA] arreglo de jugadores null debe lanzar IllegalArgumentException")
	void jugadoresNullLanzaExcepcion() {
		// Hoy: NullPointerException
		assertThrows(IllegalArgumentException.class, () -> new Juego(null));
	}

	@Test
	@DisplayName("al iniciar el turno hay tiros y volteos disponibles")
	void turnoInicialTieneRecursos() throws Exception {
		assertTrue(entero(juego, "tirosRestantes") > 0);
		assertTrue(entero(juego, "volteosRestantes") > 0);
	}

	// ------------------------------------------------------------------
	// realizarTiro
	// ------------------------------------------------------------------
	@Test
	@DisplayName("el primer tiro devuelve 5 dados entre 1 y 6")
	void primerTiroDevuelveCincoDadosValidos() {
		int[] dados = juego.realizarTiro(new int[0]);
		assertEquals(5, dados.length);
		for (int d : dados) assertTrue(d >= 1 && d <= 6, "dado fuera de rango: " + d);
	}

	@Test
	@DisplayName("cada tiro consume exactamente un tiro")
	void cadaTiroConsumeUno() throws Exception {
		int antes = entero(juego, "tirosRestantes");
		juego.realizarTiro(new int[0]);
		assertEquals(antes - 1, entero(juego, "tirosRestantes"));
	}

	@Test
	@DisplayName("agotados los tiros, los dados ya no cambian y el contador no baja de 0")
	void noSePuedeTirarMasAllaDelLimite() throws Exception {
		juego.realizarTiro(new int[0]);
		for (int i = 0; i < 10; i++) juego.realizarTiro(RELANZAR);
		assertEquals(0, entero(juego, "tirosRestantes"));

		int[] antes = juego.realizarTiro(RELANZAR).clone();
		int[] despues = juego.realizarTiro(RELANZAR);
		assertArrayEquals(antes, despues);
		assertEquals(0, entero(juego, "tirosRestantes"));
	}

	@Test
	@DisplayName("[PUEDE FALLAR] realizarTiro devuelve una copia: modificarla no altera los dados del cubilete")
	void tiroDevuelveCopiaDeLosDados() {
		juego.realizarTiro(new int[0]);
		for (int i = 0; i < 5; i++) juego.realizarTiro(RELANZAR);

		int[] devuelto = juego.realizarTiro(RELANZAR);
		int original = devuelto[0];
		devuelto[0] = 99;
		assertEquals(original, juego.realizarTiro(RELANZAR)[0]);
	}

	// ------------------------------------------------------------------
	// realizarVolteo
	// ------------------------------------------------------------------
	@Test
	@DisplayName("[FALLA ESPERADA] no se puede voltear antes del primer tiro")
	void noSePuedeVoltearAntesDeTirar() {
		assertFalse(juego.realizarVolteo(VOLTEAR));
	}

	@Test
	@DisplayName("se permiten exactamente tantos volteos como el límite")
	void soloSePermitenLosVolteosDelLimite() throws Exception {
		juego.realizarTiro(new int[0]);
		int limite = entero(juego, "volteosRestantes");
		for (int i = 0; i < limite; i++) assertTrue(juego.realizarVolteo(VOLTEAR), "volteo " + (i + 1));
		assertFalse(juego.realizarVolteo(VOLTEAR));
		assertEquals(0, entero(juego, "volteosRestantes"));
	}

	@Test
	@DisplayName("voltear no consume tiros")
	void volteoNoConsumeTiros() throws Exception {
		juego.realizarTiro(new int[0]);
		int antes = entero(juego, "tirosRestantes");
		juego.realizarVolteo(VOLTEAR);
		assertEquals(antes, entero(juego, "tirosRestantes"));
	}

	// ------------------------------------------------------------------
	// anotarTurno
	// ------------------------------------------------------------------
	@Test
	@DisplayName("[FALLA ESPERADA] no se puede anotar sin haber tirado")
	void noSePuedeAnotarSinTirar() throws Exception {
		assertFalse(juego.anotarTurno("Balas"));
		assertSame(jugadores(juego)[0], juego.getJugadorActual(), "el turno no debe avanzar");
	}

	@Test
	@DisplayName("[FALLA ESPERADA] casilla inexistente devuelve false y no avanza el turno")
	void casillaInexistenteNoAvanzaElTurno() throws Exception {
		tirarSinMano(juego);
		// Hoy: ArrayIndexOutOfBoundsException desde EvaluadorCacho
		assertFalse(juego.anotarTurno("Inexistente"));
		assertSame(jugadores(juego)[0], juego.getJugadorActual());
	}

	@Test
	@DisplayName("anotar una casilla libre devuelve true y pasa el turno al siguiente jugador")
	void anotarCasillaValidaPasaElTurno() throws Exception {
		tirarSinMano(juego);
		assertTrue(juego.anotarTurno("Balas"));
		assertSame(jugadores(juego)[1], juego.getJugadorActual());
	}

	@Test
	@DisplayName("los turnos rotan de forma circular")
	void turnosCirculares() throws Exception {
		tirarSinMano(juego);
		juego.anotarTurno("Balas");
		tirarSinMano(juego);
		juego.anotarTurno("Balas");
		assertSame(jugadores(juego)[0], juego.getJugadorActual());
	}

	@Test
	@DisplayName("al cambiar de turno se reinician tiros y volteos")
	void cambioDeTurnoReiniciaRecursos() throws Exception {
		int tiros = entero(juego, "tirosRestantes");
		int volteos = entero(juego, "volteosRestantes");

		tirarSinMano(juego);
		juego.realizarVolteo(VOLTEAR);
		juego.anotarTurno("Balas");

		assertEquals(tiros, entero(juego, "tirosRestantes"));
		assertEquals(volteos, entero(juego, "volteosRestantes"));
	}

	@Test
	@DisplayName("no se puede anotar dos veces la misma casilla y el turno no avanza")
	void noPermiteRepetirCasilla() throws Exception {
		tirarSinMano(juego);
		juego.anotarTurno("Balas"); // Ana
		tirarSinMano(juego);
		juego.anotarTurno("Balas"); // Beto
		tirarSinMano(juego); // Ana otra vez
		assertFalse(juego.anotarTurno("Balas"));
		assertSame(jugadores(juego)[0], juego.getJugadorActual());
	}

	@ParameterizedTest(name = "[FALLA ESPERADA] {0} sin la combinación no suma puntos")
	@ValueSource(strings = { "Escalera", "Full", "Poker", "Grande 1", "Grande 2" })
	void combinacionNoFormadaNoSumaPuntos(String casilla) {
		Juego j = juegoSinCombinaciones();
		Jugador actual = j.getJugadorActual();
		// Hoy anotarTurno() nunca llama a verificarEscalera/Full/Poker/Grande,
		// así que se anotan 20/30/40/50 con cualquier tirada.
		j.anotarTurno(casilla);
		assertEquals(0, actual.getTablero().calcularPuntajeTotal());
	}
	
}
