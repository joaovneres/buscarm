package com.buscarm.usuario.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.usuario.application.port.in.AutenticarUseCase;
import com.buscarm.usuario.application.port.out.CodificadorSenhaPort;
import com.buscarm.usuario.application.port.out.GeradorTokenPort;
import com.buscarm.usuario.application.port.out.UsuarioRepositoryPort;
import com.buscarm.usuario.domain.Usuario;
import com.buscarm.usuario.domain.exception.CredenciaisInvalidasException;

@Service
public class AutenticacaoService implements AutenticarUseCase {

    private final UsuarioRepositoryPort repositorio;
    private final CodificadorSenhaPort codificador;
    private final GeradorTokenPort gerador;

    public AutenticacaoService(UsuarioRepositoryPort repositorio,
                               CodificadorSenhaPort codificador,
                               GeradorTokenPort gerador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.gerador = gerador;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoLogin autenticar(String email, String senha) {
        Usuario usuario = repositorio.buscarPorEmail(email == null ? "" : email.trim().toLowerCase())
                .filter(Usuario::isAtivo)
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!codificador.confere(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return new ResultadoLogin(gerador.gerar(usuario), usuario.getNome(),
                usuario.getEmail(), usuario.getPapel().name());
    }
}
