package com.buscarm.importacao.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class ValidadorRegistroTest {

    @Test
    void validoQuandoRmENomePresentesESemDuplicidade() {
        var r = ValidadorRegistro.classificar(new RegistroExtraido(2, 203, "Sousa, João"), Optional.empty());
        assertThat(r.status()).isEqualTo(StatusItem.VALIDO);
    }

    @Test
    void invalidoQuandoRmAusente() {
        var r = ValidadorRegistro.classificar(new RegistroExtraido(3, null, "Souza, Lara"), Optional.empty());
        assertThat(r.status()).isEqualTo(StatusItem.INVALIDO);
    }

    @Test
    void invalidoQuandoNomeAusente() {
        var r = ValidadorRegistro.classificar(new RegistroExtraido(4, 503, "  "), Optional.empty());
        assertThat(r.status()).isEqualTo(StatusItem.INVALIDO);
    }

    @Test
    void duplicadoQuandoMesmoRmEMesmoNome() {
        var r = ValidadorRegistro.classificar(
                new RegistroExtraido(5, 503, "Souza, Lara"), Optional.of("souza, lara"));
        assertThat(r.status()).isEqualTo(StatusItem.DUPLICADO);
    }

    @Test
    void conflitoQuandoMesmoRmENomeDiferente() {
        var r = ValidadorRegistro.classificar(
                new RegistroExtraido(6, 503, "Souza, Laura"), Optional.of("souza, lara"));
        assertThat(r.status()).isEqualTo(StatusItem.CONFLITO);
    }
}
