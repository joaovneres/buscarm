package com.buscarm.comum;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NomeNormalizerTest {

    @Test
    void removeAcentosEBaixaCaixa() {
        assertThat(NomeNormalizer.normalizar("Sousa, João Victor"))
                .isEqualTo("sousa, joao victor");
    }

    @Test
    void colapsaEspacos() {
        assertThat(NomeNormalizer.normalizar("  Souza,   Lara  "))
                .isEqualTo("souza, lara");
    }

    @Test
    void extraiSobrenomeAntesDaVirgula() {
        assertThat(NomeNormalizer.extrairSobrenome("Sousa, João Victor Cosme Neres de"))
                .isEqualTo("Sousa");
    }

    @Test
    void semVirgulaRetornaNomeInteiroComoSobrenome() {
        assertThat(NomeNormalizer.extrairSobrenome("Maria Aparecida"))
                .isEqualTo("Maria Aparecida");
    }
}
