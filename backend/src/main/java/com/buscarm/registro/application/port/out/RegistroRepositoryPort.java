package com.buscarm.registro.application.port.out;

import java.util.Optional;

import com.buscarm.registro.domain.Registro;
import com.buscarm.shared.Pagina;

/** Porta de saída: persistência de registros (implementada por um adaptador de infra). */
public interface RegistroRepositoryPort {

    boolean existePorRm(int rm);

    Registro salvar(Registro registro);

    Optional<Registro> buscarPorId(long id);

    Optional<Registro> buscarPorRmAtivo(int rm);

    Pagina<Registro> buscarPorNome(String nomeNormalizado, int pagina, int tamanho);

    Pagina<Registro> listarAtivos(int pagina, int tamanho);
}
