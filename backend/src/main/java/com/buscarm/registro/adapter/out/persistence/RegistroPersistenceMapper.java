package com.buscarm.registro.adapter.out.persistence;

import com.buscarm.registro.domain.Registro;

/** Conversão entre o modelo de domínio e a entidade JPA. */
final class RegistroPersistenceMapper {

    private RegistroPersistenceMapper() {
    }

    static Registro paraDominio(RegistroJpaEntity e) {
        return Registro.reconstituir(
                e.getId(),
                e.getRm(),
                e.getNomeCompleto(),
                e.getSobrenome(),
                e.getNomeBusca(),
                e.getAnoRegistro(),
                e.isAtivo(),
                e.getCriadoPor(),
                e.getAtualizadoPor(),
                e.getCriadoEm()
        );
    }

    /** Cria a entidade para persistência (autoria e datas são preenchidas pelo JPA Auditing). */
    static RegistroJpaEntity paraNovaEntidade(Registro r) {
        RegistroJpaEntity e = new RegistroJpaEntity();
        e.setRm(r.getRm());
        e.setNomeCompleto(r.getNomeCompleto());
        e.setSobrenome(r.getSobrenome());
        e.setNomeBusca(r.getNomeBusca());
        e.setAnoRegistro(r.getAnoRegistro());
        e.setAtivo(r.isAtivo());
        return e;
    }
}
