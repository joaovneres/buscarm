package com.buscarm.usuario.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.buscarm.usuario.application.port.out.UsuarioRepositoryPort;
import com.buscarm.usuario.domain.Usuario;
import com.buscarm.usuario.domain.exception.UsuarioNaoEncontradoException;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioSpringDataRepository repository;

    UsuarioPersistenceAdapter(UsuarioSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return repository.findByUsername(username).map(UsuarioPersistenceAdapter::paraDominio);
    }

    @Override
    public boolean existeAlgum() {
        return repository.count() > 0;
    }

    @Override
    public List<Usuario> listarTodos() {
        return repository.findAll(Sort.by("nome").ascending()).stream()
                .map(UsuarioPersistenceAdapter::paraDominio)
                .toList();
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioJpaEntity salvo = repository.save(new UsuarioJpaEntity(
                usuario.getNome(), usuario.getUsername(), usuario.getSenhaHash(),
                usuario.getPapel(), usuario.isAtivo(), usuario.isSenhaProvisoria()));
        return paraDominio(salvo);
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return repository.findById(id).map(UsuarioPersistenceAdapter::paraDominio);
    }

    @Override
    public Usuario atualizar(Usuario usuario) {
        UsuarioJpaEntity e = repository.findById(usuario.getId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuario.getId()));
        e.setNome(usuario.getNome());
        e.setPapel(usuario.getPapel());
        e.setAtivo(usuario.isAtivo());
        e.setSenhaHash(usuario.getSenhaHash());
        e.setSenhaProvisoria(usuario.isSenhaProvisoria());
        return paraDominio(repository.save(e));
    }

    private static Usuario paraDominio(UsuarioJpaEntity e) {
        return Usuario.reconstituir(e.getId(), e.getNome(), e.getUsername(), e.getSenhaHash(),
                e.getPapel(), e.isAtivo(), e.isSenhaProvisoria());
    }
}
