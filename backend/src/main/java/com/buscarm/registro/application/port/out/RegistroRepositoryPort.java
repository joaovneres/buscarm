package com.buscarm.registro.application.port.out;

import java.util.List;
import java.util.Optional;

import com.buscarm.registro.domain.FiltroRegistros;
import com.buscarm.registro.domain.Registro;
import com.buscarm.shared.Pagina;

/** Porta de saída: persistência de registros (implementada por um adaptador de infra). */
public interface RegistroRepositoryPort {

    boolean existePorRm(int rm);

    Registro salvar(Registro registro);

    /** Atualiza um registro já existente (preserva autoria de criação; JPA registra quem alterou). */
    Registro atualizar(Registro registro);

    /** Todos os registros ativos (para exportação/backup), ordenados por sobrenome. */
    List<Registro> listarTodosAtivos();

    Optional<Registro> buscarPorId(long id);

    /** Busca paginada de ativos aplicando os filtros (nome/RM/status/ano/inicial), ordenada por sobrenome. */
    Pagina<Registro> pesquisar(FiltroRegistros filtro, int pagina, int tamanho);
}
