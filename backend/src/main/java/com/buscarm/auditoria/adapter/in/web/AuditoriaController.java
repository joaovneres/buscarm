package com.buscarm.auditoria.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buscarm.auditoria.adapter.in.web.dto.AuditoriaResponse;
import com.buscarm.auditoria.application.port.in.ConsultarAuditoriaUseCase;
import com.buscarm.shared.Pagina;

/** Consulta da auditoria — acesso restrito a ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final ConsultarAuditoriaUseCase consultarAuditoria;

    public AuditoriaController(ConsultarAuditoriaUseCase consultarAuditoria) {
        this.consultarAuditoria = consultarAuditoria;
    }

    @GetMapping
    public Pagina<AuditoriaResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarAuditoria.listar(pagina, tamanho).map(a -> new AuditoriaResponse(
                a.getId(), a.getUsuario(), a.getAcao(), a.getEntidade(),
                a.getEntidadeId(), a.getDetalhe(), a.getCriadoEm()));
    }
}
