package com.buscarm.importacao.adapter.in.web.dto;

import java.util.List;

import com.buscarm.importacao.domain.Importacao;
import com.buscarm.importacao.domain.ItemImportacao;
import com.buscarm.importacao.domain.StatusItem;

public final class ImportacaoWebMapper {

    private ImportacaoWebMapper() {
    }

    public static ImportacaoResponse paraResponse(Importacao imp) {
        List<ImportacaoResponse.Item> itens = imp.getItens().stream()
                .map(ImportacaoWebMapper::item)
                .toList();
        return new ImportacaoResponse(
                imp.getId(),
                imp.getTipo().name(),
                imp.getNomeArquivo(),
                imp.getStatus().name(),
                resumo(imp),
                itens
        );
    }

    private static ImportacaoResponse.Item item(ItemImportacao i) {
        return new ImportacaoResponse.Item(
                i.getLinhaOrigem(),
                i.getRmExtraido(),
                i.getNomeExtraido(),
                i.getStatus().name(),
                i.getMensagemErro(),
                i.getRegistroId()
        );
    }

    private static ImportacaoResponse.Resumo resumo(Importacao imp) {
        return new ImportacaoResponse.Resumo(
                imp.getQuantidadeTotal(),
                imp.contar(StatusItem.VALIDO),
                imp.contar(StatusItem.DUPLICADO),
                imp.contar(StatusItem.CONFLITO),
                imp.contar(StatusItem.INVALIDO),
                imp.contar(StatusItem.IMPORTADO),
                imp.contar(StatusItem.IGNORADO)
        );
    }
}
