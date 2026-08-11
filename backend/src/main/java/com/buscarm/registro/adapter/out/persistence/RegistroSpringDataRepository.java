package com.buscarm.registro.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Repositório Spring Data (detalhe de infraestrutura, usado pelo adaptador). */
interface RegistroSpringDataRepository
        extends JpaRepository<RegistroJpaEntity, Long>, JpaSpecificationExecutor<RegistroJpaEntity> {

    boolean existsByRm(Integer rm);

    Optional<RegistroJpaEntity> findByRm(Integer rm);

    List<RegistroJpaEntity> findByAtivoTrueOrderBySobrenomeAsc();
}
