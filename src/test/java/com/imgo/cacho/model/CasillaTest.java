package com.imgo.cacho.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.imgo.cacho.model.Casilla.EstadoCasilla;
import com.imgo.cacho.model.Casilla.TipoCasilla;

class CasillaTest {

    private Casilla casilla;

    @BeforeEach
    void setUp() {
        casilla = new Casilla("Ases", TipoCasilla.NUMERICA);
    }

    // ---------- Construccion ----------

    @Test
    @DisplayName("Una casilla nueva esta vacia, sin puntos y sin mano")
    void estadoInicial() {
        assertEquals("Ases", casilla.getNombre());
        assertEquals(EstadoCasilla.VACIO, casilla.getEstado());
        assertEquals(0, casilla.getPuntos());
        assertFalse(casilla.esMano());
    }

    @Test
    @DisplayName("esCasillaEspecial distingue el tipo de casilla")
    void esCasillaEspecialSegunTipo() {
        assertFalse(casilla.esCasillaEspecial());
        assertTrue(new Casilla("Escalera", TipoCasilla.ESPECIAL).esCasillaEspecial());
    }

    // ---------- Anotar ----------

    @Test
    @DisplayName("Anotar en casilla vacia: true, queda LLENO con los puntos")
    void anotarEnVacia() {
        assertTrue(casilla.anotar(3, false));
        assertEquals(EstadoCasilla.LLENO, casilla.getEstado());
        assertEquals(3, casilla.getPuntos());
        assertFalse(casilla.esMano());
    }

    @Test
    @DisplayName("Anotar de mano guarda la marca de mano")
    void anotarDeMano() {
        assertTrue(casilla.anotar(5, true));
        assertTrue(casilla.esMano());
    }

    @Test
    @DisplayName("Anotar 0 puntos es valido y la casilla queda LLENO (no TACHADO)")
    void anotarCeroPuntos() {
        assertTrue(casilla.anotar(0, false));
        assertEquals(EstadoCasilla.LLENO, casilla.getEstado());
        assertEquals(0, casilla.getPuntos());
    }

    @Test
    @DisplayName("Anotar en casilla ya llena: false y no cambia nada")
    void anotarEnLlenaFalla() {
        casilla.anotar(3, true);
        assertFalse(casilla.anotar(10, false));
        assertEquals(3, casilla.getPuntos());
        assertTrue(casilla.esMano());
        assertEquals(EstadoCasilla.LLENO, casilla.getEstado());
    }

    @Test
    @DisplayName("Anotar en casilla tachada: false y sigue TACHADO")
    void anotarEnTachadaFalla() {
        casilla.tachar();
        assertFalse(casilla.anotar(10, true));
        assertEquals(EstadoCasilla.TACHADO, casilla.getEstado());
        assertEquals(0, casilla.getPuntos());
        assertFalse(casilla.esMano());
    }

    // ---------- Tachar ----------

    @Test
    @DisplayName("Tachar casilla vacia: true, queda TACHADO con 0 puntos")
    void tacharEnVacia() {
        assertTrue(casilla.tachar());
        assertEquals(EstadoCasilla.TACHADO, casilla.getEstado());
        assertEquals(0, casilla.getPuntos());
        assertFalse(casilla.esMano());
    }

    @Test
    @DisplayName("Tachar casilla llena: false y conserva sus puntos")
    void tacharEnLlenaFalla() {
        casilla.anotar(4, true);
        assertFalse(casilla.tachar());
        assertEquals(EstadoCasilla.LLENO, casilla.getEstado());
        assertEquals(4, casilla.getPuntos());
        assertTrue(casilla.esMano());
    }

    @Test
    @DisplayName("Tachar dos veces: la segunda devuelve false")
    void tacharDosVeces() {
        assertTrue(casilla.tachar());
        assertFalse(casilla.tachar());
        assertEquals(EstadoCasilla.TACHADO, casilla.getEstado());
    }

    // ---------- Reiniciar ----------

    @Test
    @DisplayName("Reiniciar una casilla llena la deja como nueva")
    void reiniciarDesdeLlena() {
        casilla.anotar(5, true);
        casilla.reiniciar();
        assertEquals(EstadoCasilla.VACIO, casilla.getEstado());
        assertEquals(0, casilla.getPuntos());
        assertFalse(casilla.esMano());
    }

    @Test
    @DisplayName("Reiniciar una casilla tachada la deja como nueva")
    void reiniciarDesdeTachada() {
        casilla.tachar();
        casilla.reiniciar();
        assertEquals(EstadoCasilla.VACIO, casilla.getEstado());
    }

    @Test
    @DisplayName("Tras reiniciar se puede volver a anotar")
    void anotarTrasReiniciar() {
        casilla.anotar(5, false);
        casilla.reiniciar();
        assertTrue(casilla.anotar(2, true));
        assertEquals(2, casilla.getPuntos());
        assertTrue(casilla.esMano());
    }
    @Test
    @DisplayName("Anotar puntos negativos, fuerza cero puntos")
    void anotarPuntosNegativosNoSePermite() {
        assertTrue(casilla.anotar(-5, false));
        assertEquals(EstadoCasilla.LLENO, casilla.getEstado());
        assertEquals(0, casilla.getPuntos());
    }

    @Test
    @DisplayName("El nombre de una casilla no puede ser null")
    void constructorNombreNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Casilla(null, TipoCasilla.NUMERICA));
    }

    @Test
    @DisplayName("El tipo de una casilla no puede ser null")
    void constructorTipoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Casilla("Ases", null));
    }
}
