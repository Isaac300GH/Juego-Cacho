package com.imgo.cacho.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Asume que Juego.nombresCasillas es un String[] de 11 nombres:
 * los 6 primeros numericos y los 5 ultimos especiales.
 */
class TableroTest {

    private static final int TOTAL_CASILLAS = 11;

    private Tablero tablero;

    @BeforeEach
    void setUp() {
        tablero = new Tablero();
    }

    private static String nombre(int i) {
        return Juego.nombresCasillas[i];
    }

    // ---------- Dependencia: Juego.nombresCasillas ----------

    @Test
    @DisplayName("Juego.nombresCasillas tiene 11 nombres unicos y no vacios")
    void nombresDeCasillasValidos() {
        assertEquals(TOTAL_CASILLAS, Juego.nombresCasillas.length);
        assertEquals(TOTAL_CASILLAS, new HashSet<>(Arrays.asList(Juego.nombresCasillas)).size());
        for (String n : Juego.nombresCasillas) {
            assertTrue(n != null && !n.isBlank());
        }
    }

    // ---------- Estado inicial ----------

    @Test
    @DisplayName("Un tablero nuevo tiene puntaje 0 y no esta lleno")
    void estadoInicial() {
        assertEquals(0, tablero.getPuntaje());
        assertEquals(0, tablero.calcularPuntajeTotal());
        assertFalse(tablero.estaLleno());
    }

    // ---------- Anotar ----------

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
    @DisplayName("Se puede anotar en cada una de las 11 casillas por su nombre")
    void anotarEnCadaCasilla(int indice) {
        assertTrue(tablero.anotaraCasilla(nombre(indice), 5, false));
    }

    @Test
    @DisplayName("Anotar dos veces en la misma casilla: la segunda falla y no cambia el puntaje")
    void anotarDosVecesMismaCasilla() {
        assertTrue(tablero.anotaraCasilla(nombre(0), 3, false));
        assertFalse(tablero.anotaraCasilla(nombre(0), 10, false));
        assertEquals(3, tablero.calcularPuntajeTotal());
    }

    @Test
    @DisplayName("Anotar en una casilla no bloquea las demas")
    void casillasIndependientes() {
        assertTrue(tablero.anotaraCasilla(nombre(0), 1, false));
        assertTrue(tablero.anotaraCasilla(nombre(1), 2, false));
    }

    // ---------- Tachar ----------

    @Test
    @DisplayName("Tachar una casilla libre funciona; tachar otra vez falla")
    void tacharCasilla() {
        assertTrue(tablero.tacharCasilla(nombre(2)));
        assertFalse(tablero.tacharCasilla(nombre(2)));
    }

    @Test
    @DisplayName("No se puede anotar en una casilla tachada")
    void anotarTrasTacharFalla() {
        tablero.tacharCasilla(nombre(2));
        assertFalse(tablero.anotaraCasilla(nombre(2), 5, false));
        assertEquals(0, tablero.calcularPuntajeTotal());
    }

    @Test
    @DisplayName("No se puede tachar una casilla ya anotada y se conservan sus puntos")
    void tacharTrasAnotarFalla() {
        tablero.anotaraCasilla(nombre(2), 6, false);
        assertFalse(tablero.tacharCasilla(nombre(2)));
        assertEquals(6, tablero.calcularPuntajeTotal());
    }

    // ---------- Casillas inexistentes ----------

    @Test
    @DisplayName("Anotar en una casilla que no existe lanza IllegalArgumentException")
    void anotarCasillaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> tablero.anotaraCasilla("NoExiste", 5, false));
    }

    @Test
    @DisplayName("Tachar una casilla que no existe lanza IllegalArgumentException")
    void tacharCasillaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> tablero.tacharCasilla("NoExiste"));
    }

    // ---------- Puntaje ----------

    @Test
    @DisplayName("calcularPuntajeTotal suma las anotadas e ignora las tachadas")
    void calcularPuntajeTotal() {
        tablero.anotaraCasilla(nombre(0), 10, false);
        tablero.anotaraCasilla(nombre(3), 20, true);
        tablero.tacharCasilla(nombre(5));
        assertEquals(30, tablero.calcularPuntajeTotal());
    }

    @Test
    @DisplayName("calcularPuntajeTotal actualiza getPuntaje")
    void calcularActualizaGetPuntaje() {
        tablero.anotaraCasilla(nombre(0), 10, false);
        int total = tablero.calcularPuntajeTotal();
        assertEquals(total, tablero.getPuntaje());
    }

    @Test
    @DisplayName("calcularPuntajeTotal es idempotente")
    void calcularEsIdempotente() {
        tablero.anotaraCasilla(nombre(0), 10, false);
        assertEquals(tablero.calcularPuntajeTotal(), tablero.calcularPuntajeTotal());
    }

    // ---------- estaLleno ----------

    @Test
    @DisplayName("Con las 11 casillas anotadas el tablero esta lleno")
    void llenoTodoAnotado() {
        for (int i = 0; i < TOTAL_CASILLAS; i++) {
            tablero.anotaraCasilla(nombre(i), i, false);
        }
        assertTrue(tablero.estaLleno());
    }

    @Test
    @DisplayName("Con 10 de 11 casillas usadas no esta lleno")
    void noLlenoSiFaltaUna() {
        for (int i = 0; i < TOTAL_CASILLAS - 1; i++) {
            tablero.anotaraCasilla(nombre(i), i, false);
        }
        assertFalse(tablero.estaLleno());
    }

    @Test
    @DisplayName("Con casillas anotadas y tachadas (ninguna vacia) el tablero esta lleno")
    void llenoConAnotadasYTachadas() {
        for (int i = 0; i < TOTAL_CASILLAS; i++) {
            if (i % 2 == 0) {
                tablero.anotaraCasilla(nombre(i), i, false);
            } else {
                tablero.tacharCasilla(nombre(i));
            }
        }
        assertTrue(tablero.estaLleno());
    }

    @Test
    @DisplayName("Con las 11 casillas tachadas el tablero esta lleno")
    void llenoTodoTachado() {
        for (int i = 0; i < TOTAL_CASILLAS; i++) {
            tablero.tacharCasilla(nombre(i));
        }
        assertTrue(tablero.estaLleno());
    }
    @Test
    @DisplayName("Anotar con nombre null lanza IllegalArgumentException")
    void anotarNombreNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> tablero.anotaraCasilla(null, 5, false));
    }

    @Test
    @DisplayName("getPuntaje se actualiza al anotar, sin llamar a calcularPuntajeTotal")
    void getPuntajeSeActualizaAlAnotar() {
        tablero.anotaraCasilla(nombre(0), 5, false);
        assertEquals(5, tablero.getPuntaje());
    }
}
