package com.buscarm.usuario.adapter.in.web;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.buscarm.shared.UsuarioAtualPort;
import com.buscarm.usuario.adapter.in.web.dto.TrocarSenhaRequest;
import com.buscarm.usuario.application.port.in.TrocarSenhaUseCase;

/** Autosserviço da conta do usuário logado (qualquer papel autenticado). */
@RestController
@RequestMapping("/api/conta")
public class ContaController {

    private final TrocarSenhaUseCase trocarSenha;
    private final UsuarioAtualPort usuarioAtual;

    public ContaController(TrocarSenhaUseCase trocarSenha, UsuarioAtualPort usuarioAtual) {
        this.trocarSenha = trocarSenha;
        this.usuarioAtual = usuarioAtual;
    }

    /** O próprio usuário troca a sua senha (informando a atual). */
    @PatchMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trocarSenha(@Valid @RequestBody TrocarSenhaRequest req) {
        trocarSenha.trocarSenha(usuarioAtual.getUsuarioAtual(), req.senhaAtual(), req.novaSenha());
    }
}
