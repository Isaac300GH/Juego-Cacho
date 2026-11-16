package com.imgo.cacho.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CubileteTest {

    private Cubilete cubilete;

    @BeforeEach
    void setUp() {
        cubilete = new Cubilete();
    }

    private static void assertTodosEnRango(int[] dados) {
        for (int d : dados) {
            assertTrue(d >= 1 && d <= 6, "Dado fuera de rango: " + d);
        }
    }

    // ---------- Estado inicial ----------

    @Test
    @DisplayName("Un cubilete nuevo tiene 5 dados")
    void tieneCincoDados() {
        assertEquals(5, cubilete.getDados().length);
    }

    @Test
    @DisplayName("Los dados de un cubilete nuevo valen 1")
    void dadosInicialesValenUno() {
        assertArrayEquals(new int[] { 1, 1, 1, 1, 1 }, cubilete.getDados());
    }

    @Test
    @DisplayName("Un cubilete nuevo empieza como mano")
    void empiezaComoMano() {
        assertTrue(cubilete.isMano());
    }

    @Test
    @DisplayName("getDados devuelve una copia: modificarla no altera el cubilete")
    void getDadosDevuelveCopia() {
        int[] copia = cubilete.getDados();
        copia[0] = 6;
        assertEquals(1, cubilete.getDados()[0]);
    }

    // ---------- Tirar ----------

    @RepeatedTest(100)
    @DisplayName("tirarTodos deja los 5 dados con valores entre 1 y 6")
    void tirarTodosDaValoresEnRango() {
        cubilete.tirarTodos();
        int[] dados = cubilete.getDados();
        assertEquals(5, dados.length);
        assertTodosEnRango(dados);
    }

    @RepeatedTest(50)
    @DisplayName("tirarCubilete solo modifica los dados indicados")
    void tirarCubileteSoloAfectaIndicados() {
        cubilete.tirarTodos();
        int[] antes = cubilete.getDados();

        cubilete.tirarCubilete(new int[] { 0, 2 });
        int[] despues = cubilete.getDados();

        assertEquals(antes[1], despues[1]);
        assertEquals(antes[3], despues[3]);
        assertEquals(antes[4], despues[4]);
        assertTodosEnRango(despues);
    }

    @Test
    @DisplayName("tirarCubilete con arreglo vacio no cambia nada")
    void tirarCubileteVacioNoCambiaNada() {
        cubilete.tirarTodos();
        int[] antes = cubilete.getDados();
        cubilete.tirarCubilete(new int[] {});
        assertArrayEquals(antes, cubilete.getDados());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 5, 10 })
    @DisplayName("tirarCubilete con indice invalido lanza excepcion de indice")
    void tirarCubileteIndiceInvalido(int indice) {
        assertThrows(IndexOutOfBoundsException.class,
                () -> cubilete.tirarCubilete(new int[] { indice }));
    }

    // ---------- Voltear ----------

    @RepeatedTest(50)
    @DisplayName("voltearDados invierte solo el dado indicado")
    void voltearDadosSoloAfectaAlIndicado() {
        cubilete.tirarTodos();
        int[] antes = cubilete.getDados();

        cubilete.voltearDados(3);
        int[] despues = cubilete.getDados();

        assertEquals(7 - antes[3], despues[3]);
        assertEquals(antes[0], despues[0]);
        assertEquals(antes[1], despues[1]);
        assertEquals(antes[2], despues[2]);
        assertEquals(antes[4], despues[4]);
    }

    @Test
    @DisplayName("Voltear dos veces el mismo dado restaura su valor")
    void voltearDosVecesRestaura() {
        cubilete.tirarTodos();
        int[] antes = cubilete.getDados();
        cubilete.voltearDados(1);
        cubilete.voltearDados(1);
        assertArrayEquals(antes, cubilete.getDados());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 5, 10 })
    @DisplayName("voltearDados con indice invalido lanza excepcion de indice")
    void voltearDadosIndiceInvalido(int indice) {
        assertThrows(IndexOutOfBoundsException.class, () -> cubilete.voltearDados(indice));
    }

    // ---------- Mano ----------

    @Test
    @DisplayName("cambioMano actualiza el estado de mano")
    void cambioManoActualizaEstado() {
        cubilete.cambioMano(false);
        assertFalse(cubilete.isMano());
        cubilete.cambioMano(true);
        assertTrue(cubilete.isMano());
    }

}
