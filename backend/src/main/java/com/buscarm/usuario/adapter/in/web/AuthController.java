package com.buscarm.usuario.adapter.in.web;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buscarm.usuario.adapter.in.web.dto.LoginRequest;
import com.buscarm.usuario.adapter.in.web.dto.LoginResponse;
import com.buscarm.usuario.application.port.in.AutenticarUseCase;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticarUseCase autenticar;

    public AuthController(AutenticarUseCase autenticar) {
        this.autenticar = autenticar;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        var r = autenticar.autenticar(req.username(), req.senha());
        return new LoginResponse(r.token(), r.nome(), r.username(), r.papel(), r.senhaProvisoria());
    }
}
