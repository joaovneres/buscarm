package com.buscarm.registro;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buscarm.comum.PaginaResponse;
import com.buscarm.registro.dto.RegistroRequest;
import com.buscarm.registro.dto.RegistroResponse;

@RestController
@RequestMapping("/api/registros")
public class RegistroController {

    private final RegistroService service;

    public RegistroController(RegistroService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RegistroResponse> criar(@Valid @RequestBody RegistroRequest req) {
        RegistroResponse resp = service.criar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public RegistroResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    /** Pesquisa por nome (parcial, acento-insensível) OU por RM. */
    @GetMapping
    public PaginaResponse<RegistroResponse> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer rm,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.pesquisar(nome, rm, pagina, tamanho);
    }
}
