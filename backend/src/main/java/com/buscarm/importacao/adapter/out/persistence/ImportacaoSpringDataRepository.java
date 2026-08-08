package com.buscarm.importacao.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ImportacaoSpringDataRepository extends JpaRepository<ImportacaoJpaEntity, Long> {
}
