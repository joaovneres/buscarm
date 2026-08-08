package com.buscarm.importacao.adapter.out.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.buscarm.importacao.application.port.out.ImportacaoRepositoryPort;
import com.buscarm.importacao.domain.Importacao;
import com.buscarm.importacao.domain.ItemImportacao;
import com.buscarm.importacao.domain.StatusItem;
import com.buscarm.importacao.domain.exception.ImportacaoNaoEncontradaException;

/** Adaptador de saída da persistência de importações usando Spring Data JPA. */
@Component
public class ImportacaoPersistenceAdapter implements ImportacaoRepositoryPort {

    private final ImportacaoSpringDataRepository repository;

    ImportacaoPersistenceAdapter(ImportacaoSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Importacao salvar(Importacao dominio) {
        ImportacaoJpaEntity entity = dominio.getId() == null
                ? novaEntidade(dominio)
                : atualizarEntidade(dominio);
        aplicarContadores(entity, dominio);
        return ImportacaoPersistenceMapper.paraDominio(repository.save(entity));
    }

    @Override
    public Optional<Importacao> buscarPorId(long id) {
        return repository.findById(id).map(ImportacaoPersistenceMapper::paraDominio);
    }

    private ImportacaoJpaEntity novaEntidade(Importacao dominio) {
        ImportacaoJpaEntity entity = new ImportacaoJpaEntity();
        entity.setTipo(dominio.getTipo());
        entity.setNomeArquivo(dominio.getNomeArquivo());
        entity.setStatus(dominio.getStatus());
        for (ItemImportacao item : dominio.getItens()) {
            entity.adicionarItem(ImportacaoPersistenceMapper.novoItemEntidade(item));
        }
        return entity;
    }

    private ImportacaoJpaEntity atualizarEntidade(Importacao dominio) {
        ImportacaoJpaEntity entity = repository.findById(dominio.getId())
                .orElseThrow(() -> new ImportacaoNaoEncontradaException(dominio.getId()));
        entity.setStatus(dominio.getStatus());
        Map<Integer, ItemImportacaoJpaEntity> porLinha = entity.getItens().stream()
                .collect(Collectors.toMap(ItemImportacaoJpaEntity::getLinhaOrigem, Function.identity()));
        for (ItemImportacao item : dominio.getItens()) {
            ItemImportacaoJpaEntity alvo = porLinha.get(item.getLinhaOrigem());
            if (alvo != null) {
                alvo.setStatus(item.getStatus());
                alvo.setMensagemErro(item.getMensagemErro());
                alvo.setRegistroMatriculaId(item.getRegistroId());
            }
        }
        return entity;
    }

    private void aplicarContadores(ImportacaoJpaEntity entity, Importacao dominio) {
        entity.setQuantidadeTotal(dominio.getQuantidadeTotal());
        entity.setQuantidadeImportada((int) dominio.contar(StatusItem.IMPORTADO));
        entity.setQuantidadeIgnorada((int) dominio.contar(StatusItem.IGNORADO));
        entity.setQuantidadeComErro(
                (int) (dominio.contar(StatusItem.INVALIDO) + dominio.contar(StatusItem.CONFLITO)));
    }
}
