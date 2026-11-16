package com.imgo.cacho.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class DadoTest {

    private Dado dado;

    @BeforeEach
    void setUp() {
        dado = new Dado();
    }

    @Test
    @DisplayName("Un dado recien creado vale 1")
    void valorInicialEsUno() {
        assertEquals(1, dado.getDado());
    }

    @RepeatedTest(200)
    @DisplayName("tirarDado siempre deja un valor entre 1 y 6")
    void tirarDadoDaValorEnRango() {
        dado.tirarDado();
        int valor = dado.getDado();
        assertTrue(valor >= 1 && valor <= 6, "Valor fuera de rango: " + valor);
    }

    @Test
    @DisplayName("tirarDado puede producir las 6 caras")
    void tirarDadoProduceLasSeisCaras() {
        Set<Integer> vistos = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            dado.tirarDado();
            vistos.add(dado.getDado());
        }
        assertEquals(Set.of(1, 2, 3, 4, 5, 6), vistos);
    }

    @Test
    @DisplayName("getDado no cambia el valor entre lecturas")
    void getDadoEsEstable() {
        dado.tirarDado();
        assertEquals(dado.getDado(), dado.getDado());
    }

    @Test
    @DisplayName("Voltear un dado nuevo (1) da 6")
    void voltearDadoInicialDaSeis() {
        dado.voltear();
        assertEquals(6, dado.getDado());
    }

    @RepeatedTest(100)
    @DisplayName("voltear deja la cara opuesta: las caras suman 7")
    void voltearDaCaraOpuesta() {
        dado.tirarDado();
        int antes = dado.getDado();
        dado.voltear();
        int despues = dado.getDado();
        assertEquals(7, antes + despues);
        assertTrue(despues >= 1 && despues <= 6);
    }

    @RepeatedTest(100)
    @DisplayName("Voltear dos veces restaura el valor original")
    void voltearDosVecesRestauraValor() {
        dado.tirarDado();
        int original = dado.getDado();
        dado.voltear();
        dado.voltear();
        assertEquals(original, dado.getDado());
    }
}
