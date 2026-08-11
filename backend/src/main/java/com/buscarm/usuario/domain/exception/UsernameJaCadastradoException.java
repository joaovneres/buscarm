package com.buscarm.usuario.domain.exception;

/** Username já usado por outro usuário. */
public class UsernameJaCadastradoException extends RuntimeException {
    public UsernameJaCadastradoException(String username) {
        super("Usuário já cadastrado: " + username);
    }
}
