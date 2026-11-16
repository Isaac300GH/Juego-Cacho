package com.imgo.cacho.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JugadorTest {

    @Test
    @DisplayName("El jugador conserva su nombre")
    void conservaNombre() {
        assertEquals("Isaac", new Jugador("Isaac",1).getNombre());
    }

    @Test
    @DisplayName("Todo jugador nace con un tablero propio y vacio")
    void nacenConTableroVacio() {
        Tablero tablero = new Jugador("Isaac",1).getTablero();
        assertNotNull(tablero);
        assertEquals(0, tablero.calcularPuntajeTotal());
        assertFalse(tablero.estaLleno());
    }

    @Test
    @DisplayName("getTablero devuelve siempre la misma instancia")
    void mismoTableroEnCadaLlamada() {
        Jugador jugador = new Jugador("Isaac",1);
        assertSame(jugador.getTablero(), jugador.getTablero());
    }

    @Test
    @DisplayName("Dos jugadores no comparten tablero")
    void tablerosIndependientes() {
        Jugador a = new Jugador("A",1);
        Jugador b = new Jugador("B",1);
        assertNotSame(a.getTablero(), b.getTablero());

        String casilla = Juego.nombresCasillas[0];
        assertTrue(a.getTablero().anotaraCasilla(casilla, 5, false));
        assertEquals(5, a.getTablero().calcularPuntajeTotal());
        assertEquals(0, b.getTablero().calcularPuntajeTotal());
        // B todavia puede anotar en esa casilla
        assertTrue(b.getTablero().anotaraCasilla(casilla, 2, false));
    }
    @Test
    @DisplayName("El nombre del jugador no puede ser null")
    void nombreNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Jugador(null,1));
    }

    @Test
    @DisplayName("El nombre del jugador no puede estar vacio")
    void nombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Jugador("   ",1));
    }
}
