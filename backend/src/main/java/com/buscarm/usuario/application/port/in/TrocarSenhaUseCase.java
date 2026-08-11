package com.buscarm.usuario.application.port.in;

/** Porta de entrada: o próprio usuário troca a sua senha (limpa o flag de provisória). */
public interface TrocarSenhaUseCase {

    void trocarSenha(String username, String senhaAtual, String novaSenha);
}
