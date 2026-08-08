package com.buscarm.importacao.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;

import com.buscarm.importacao.domain.Importacao;
import com.buscarm.importacao.domain.ItemImportacao;

/** Conversão entre o agregado de domínio e as entidades JPA. */
final class ImportacaoPersistenceMapper {

    private ImportacaoPersistenceMapper() {
    }

    static Importacao paraDominio(ImportacaoJpaEntity e) {
        List<ItemImportacao> itens = new ArrayList<>();
        for (ItemImportacaoJpaEntity i : e.getItens()) {
            itens.add(new ItemImportacao(i.getId(), i.getLinhaOrigem(), i.getRmExtraido(),
                    i.getNomeExtraido(), i.getStatus(), i.getMensagemErro(), i.getRegistroMatriculaId()));
        }
        return new Importacao(e.getId(), e.getTipo(), e.getNomeArquivo(), e.getStatus(),
                itens, e.getCriadoPor(), e.getCriadoEm());
    }

    static ItemImportacaoJpaEntity novoItemEntidade(ItemImportacao it) {
        ItemImportacaoJpaEntity e = new ItemImportacaoJpaEntity();
        e.setLinhaOrigem(it.getLinhaOrigem());
        e.setRmExtraido(it.getRmExtraido());
        e.setNomeExtraido(it.getNomeExtraido());
        e.setStatus(it.getStatus());
        e.setMensagemErro(it.getMensagemErro());
        e.setRegistroMatriculaId(it.getRegistroId());
        return e;
    }
}
