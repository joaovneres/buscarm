package com.buscarm.importacao.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ExtratorLinhasOcrTest {

    @Test
    void reconheceRmNoFimDaLinha() {
        List<RegistroExtraido> r = ExtratorLinhasOcr.extrair("Sousa, João Victor Cosme Neres de   203");
        assertThat(r).hasSize(1);
        assertThat(r.get(0).rm()).isEqualTo(203);
        assertThat(r.get(0).nomeCompleto()).isEqualTo("Sousa, João Victor Cosme Neres de");
        assertThat(r.get(0).linhaOrigem()).isEqualTo(1);
    }

    @Test
    void reconheceRmNoInicioDaLinha() {
        List<RegistroExtraido> r = ExtratorLinhasOcr.extrair("204 Silva, Maria Aparecida");
        assertThat(r.get(0).rm()).isEqualTo(204);
        assertThat(r.get(0).nomeCompleto()).isEqualTo("Silva, Maria Aparecida");
    }

    @Test
    void semNumeroFicaComRmNulo() {
        List<RegistroExtraido> r = ExtratorLinhasOcr.extrair("Pereira, Carlos Eduardo");
        assertThat(r.get(0).rm()).isNull();
        assertThat(r.get(0).nomeCompleto()).isEqualTo("Pereira, Carlos Eduardo");
    }

    @Test
    void ignoraLinhasVaziasOuSemNome() {
        List<RegistroExtraido> r = ExtratorLinhasOcr.extrair("203 Ana\n\n   \n456\n204 Bruno");
        assertThat(r).hasSize(2);
        assertThat(r.get(1).linhaOrigem()).isEqualTo(5);
    }
}
