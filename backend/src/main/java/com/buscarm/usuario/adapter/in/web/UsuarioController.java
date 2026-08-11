package com.buscarm.usuario.adapter.in.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.buscarm.usuario.adapter.in.web.dto.AlterarUsuarioRequest;
import com.buscarm.usuario.adapter.in.web.dto.CriarUsuarioRequest;
import com.buscarm.usuario.adapter.in.web.dto.ResetarSenhaRequest;
import com.buscarm.usuario.adapter.in.web.dto.UsuarioResponse;
import com.buscarm.usuario.application.port.in.GerenciarUsuariosUseCase;
import com.buscarm.usuario.application.port.in.GerenciarUsuariosUseCase.AlterarUsuarioCommand;
import com.buscarm.usuario.application.port.in.GerenciarUsuariosUseCase.CriarUsuarioCommand;

/** Gestão de usuários (restrita a ADMIN via SecurityConfig). */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final GerenciarUsuariosUseCase gerenciarUsuarios;

    public UsuarioController(GerenciarUsuariosUseCase gerenciarUsuarios) {
        this.gerenciarUsuarios = gerenciarUsuarios;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest req) {
        var salvo = gerenciarUsuarios.criar(
                new CriarUsuarioCommand(req.nome(), req.username(), req.senha(), req.papel()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(salvo));
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return gerenciarUsuarios.listar().stream().map(UsuarioResponse::de).toList();
    }

    /** Altera nome e papel de um usuário. */
    @PutMapping("/{id}")
    public UsuarioResponse alterar(@PathVariable long id, @Valid @RequestBody AlterarUsuarioRequest req) {
        return UsuarioResponse.de(gerenciarUsuarios.alterar(id, new AlterarUsuarioCommand(req.nome(), req.papel())));
    }

    /** Admin redefine a senha do usuário (fica provisória até ele trocá-la). */
    @PatchMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetarSenha(@PathVariable long id, @Valid @RequestBody ResetarSenhaRequest req) {
        gerenciarUsuarios.resetarSenha(id, req.senha());
    }
}
