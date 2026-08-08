package com.buscarm.auditoria.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuditoriaSpringDataRepository extends JpaRepository<AuditoriaJpaEntity, Long> {
}
