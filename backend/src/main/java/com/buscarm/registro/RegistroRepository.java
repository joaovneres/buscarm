package com.buscarm.registro;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

    boolean existsByRm(Integer rm);

    Optional<Registro> findByRm(Integer rm);

    Page<Registro> findByAtivoTrue(Pageable pageable);

    Page<Registro> findByAtivoTrueAndNomeBuscaContaining(String termo, Pageable pageable);
}
