package com.buscarm.registro.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.buscarm.registro.application.port.out.RegistroRepositoryPort;
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
    public Optional<Registro> buscarPorRmAtivo(int rm) {
        return repository.findByRm(rm)
                .filter(RegistroJpaEntity::isAtivo)
                .map(RegistroPersistenceMapper::paraDominio);
    }

    @Override
    public Pagina<Registro> buscarPorNome(String nomeNormalizado, int pagina, int tamanho) {
        return paraPagina(repository.findByAtivoTrueAndNomeBuscaContaining(
                nomeNormalizado, ordenadoPorSobrenome(pagina, tamanho)));
    }

    @Override
    public Pagina<Registro> listarAtivos(int pagina, int tamanho) {
        return paraPagina(repository.findByAtivoTrue(ordenadoPorSobrenome(pagina, tamanho)));
    }

    private Pageable ordenadoPorSobrenome(int pagina, int tamanho) {
        return PageRequest.of(pagina, tamanho, Sort.by("sobrenome").ascending());
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
