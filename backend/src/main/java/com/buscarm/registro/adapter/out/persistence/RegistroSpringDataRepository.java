package com.buscarm.registro.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositório Spring Data (detalhe de infraestrutura, usado pelo adaptador). */
interface RegistroSpringDataRepository extends JpaRepository<RegistroJpaEntity, Long> {

    boolean existsByRm(Integer rm);

    Optional<RegistroJpaEntity> findByRm(Integer rm);

    Page<RegistroJpaEntity> findByAtivoTrue(Pageable pageable);

    Page<RegistroJpaEntity> findByAtivoTrueAndNomeBuscaContaining(String termo, Pageable pageable);
}
