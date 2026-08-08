package com.buscarm.auditoria.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.buscarm.auditoria.application.port.out.AuditoriaRepositoryPort;
import com.buscarm.auditoria.domain.RegistroAuditoria;
import com.buscarm.shared.Pagina;

@Component
public class AuditoriaPersistenceAdapter implements AuditoriaRepositoryPort {

    private final AuditoriaSpringDataRepository repository;

    AuditoriaPersistenceAdapter(AuditoriaSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public RegistroAuditoria salvar(RegistroAuditoria r) {
        AuditoriaJpaEntity salvo = repository.save(new AuditoriaJpaEntity(
                r.getUsuario(), r.getAcao(), r.getEntidade(), r.getEntidadeId(), r.getDetalhe()));
        return paraDominio(salvo);
    }

    @Override
    public Pagina<RegistroAuditoria> listar(int pagina, int tamanho) {
        Page<AuditoriaJpaEntity> page = repository.findAll(
                PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending()));
        return new Pagina<>(
                page.getContent().stream().map(AuditoriaPersistenceAdapter::paraDominio).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private static RegistroAuditoria paraDominio(AuditoriaJpaEntity e) {
        return new RegistroAuditoria(e.getId(), e.getUsuario(), e.getAcao(), e.getEntidade(),
                e.getEntidadeId(), e.getDetalhe(), e.getCriadoEm());
    }
}
