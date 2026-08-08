package com.buscarm.registro.adapter.in.web;

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

import com.buscarm.registro.adapter.in.web.dto.RegistroRequest;
import com.buscarm.registro.adapter.in.web.dto.RegistroResponse;
import com.buscarm.registro.adapter.in.web.dto.RegistroWebMapper;
import com.buscarm.registro.application.port.in.BuscarRegistrosUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase.CadastrarRegistroCommand;
import com.buscarm.shared.Pagina;

/** Adaptador de entrada (driving): expõe os casos de uso via HTTP. */
@RestController
@RequestMapping("/api/registros")
public class RegistroController {

    private final CadastrarRegistroUseCase cadastrarRegistro;
    private final BuscarRegistrosUseCase buscarRegistros;

    public RegistroController(CadastrarRegistroUseCase cadastrarRegistro,
                             BuscarRegistrosUseCase buscarRegistros) {
        this.cadastrarRegistro = cadastrarRegistro;
        this.buscarRegistros = buscarRegistros;
    }

    @PostMapping
    public ResponseEntity<RegistroResponse> criar(@Valid @RequestBody RegistroRequest req) {
        var salvo = cadastrarRegistro.cadastrar(
                new CadastrarRegistroCommand(req.rm(), req.nomeCompleto(), req.anoRegistro()));
        return ResponseEntity.status(HttpStatus.CREATED).body(RegistroWebMapper.paraResponse(salvo));
    }

    @GetMapping("/{id}")
    public RegistroResponse buscarPorId(@PathVariable long id) {
        return RegistroWebMapper.paraResponse(buscarRegistros.buscarPorId(id));
    }

    /** Pesquisa por nome (parcial, acento-insensível) OU por RM. */
    @GetMapping
    public Pagina<RegistroResponse> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer rm,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return buscarRegistros.pesquisar(nome, rm, pagina, tamanho)
                .map(RegistroWebMapper::paraResponse);
    }
}
