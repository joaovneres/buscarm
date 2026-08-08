package com.buscarm.usuario.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.buscarm.usuario.application.port.out.UsuarioRepositoryPort;
import com.buscarm.usuario.domain.Usuario;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioSpringDataRepository repository;

    UsuarioPersistenceAdapter(UsuarioSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(UsuarioPersistenceAdapter::paraDominio);
    }

    @Override
    public boolean existeAlgum() {
        return repository.count() > 0;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioJpaEntity salvo = repository.save(new UsuarioJpaEntity(
                usuario.getNome(), usuario.getEmail(), usuario.getSenhaHash(),
                usuario.getPapel(), usuario.isAtivo()));
        return paraDominio(salvo);
    }

    private static Usuario paraDominio(UsuarioJpaEntity e) {
        return Usuario.reconstituir(e.getId(), e.getNome(), e.getEmail(), e.getSenhaHash(),
                e.getPapel(), e.isAtivo());
    }
}
