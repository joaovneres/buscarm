package com.buscarm.registro.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.buscarm.registro.application.port.out.RegistroRepositoryPort;
import com.buscarm.registro.domain.FiltroRegistros;
import com.buscarm.registro.domain.NomeNormalizer;
import com.buscarm.registro.domain.Registro;
import com.buscarm.registro.domain.exception.RegistroNaoEncontradoException;
import com.buscarm.shared.Pagina;

/** Adaptador de saída que implementa a porta de persistência usando Spring Data JPA. */
@Component
public class RegistroPersistenceAdapter implements RegistroRepositoryPort {

    private final RegistroSpringDataRepository repository;

    RegistroPersistenceAdapter(RegistroSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existePorRm(int rm) {
        return repository.existsByRm(rm);
    }

    @Override
    public Registro salvar(Registro registro) {
        RegistroJpaEntity salvo = repository.save(RegistroPersistenceMapper.paraNovaEntidade(registro));
        return RegistroPersistenceMapper.paraDominio(salvo);
    }

    @Override
    public Registro atualizar(Registro registro) {
        RegistroJpaEntity e = repository.findById(registro.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException(registro.getId()));
        e.setRm(registro.getRm());
        e.setNomeCompleto(registro.getNomeCompleto());
        e.setSobrenome(registro.getSobrenome());
        e.setNomeBusca(registro.getNomeBusca());
        e.setAnoRegistro(registro.getAnoRegistro());
        e.setAtivo(registro.isAtivo());
        e.setStatus(registro.getStatus().name());
        return RegistroPersistenceMapper.paraDominio(repository.save(e));
    }

    @Override
    public List<Registro> listarTodosAtivos() {
        return repository.findByAtivoTrueOrderBySobrenomeAsc().stream()
                .map(RegistroPersistenceMapper::paraDominio)
                .toList();
    }

    @Override
    public Optional<Registro> buscarPorId(long id) {
        return repository.findById(id).map(RegistroPersistenceMapper::paraDominio);
    }

    @Override
    public Pagina<Registro> pesquisar(FiltroRegistros filtro, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("sobrenome").ascending());
        return paraPagina(repository.findAll(especificacao(filtro), pageable));
    }

    /** Compõe os critérios opcionais; nulos/vazios são ignorados. Sempre restringe a ativos. */
    private Specification<RegistroJpaEntity> especificacao(FiltroRegistros f) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.isTrue(root.get("ativo")));
            if (f.rm() != null) {
                predicados.add(cb.equal(root.get("rm"), f.rm()));
            }
            if (f.nome() != null && !f.nome().isBlank()) {
                predicados.add(cb.like(root.get("nomeBusca"), "%" + NomeNormalizer.normalizar(f.nome()) + "%"));
            }
            if (f.inicial() != null && !f.inicial().isBlank()) {
                // agrupa pela inicial do SOBRENOME (a chave do índice), acento/caixa-insensível
                predicados.add(cb.like(cb.lower(root.get("sobrenome")),
                        NomeNormalizer.normalizar(f.inicial()) + "%"));
            }
            if (f.status() != null) {
                predicados.add(cb.equal(root.get("status"), f.status().name()));
            }
            if (f.ano() != null) {
                predicados.add(cb.equal(root.get("anoRegistro"), f.ano()));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    private Pagina<Registro> paraPagina(Page<RegistroJpaEntity> page) {
        return new Pagina<>(
                page.getContent().stream().map(RegistroPersistenceMapper::paraDominio).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
