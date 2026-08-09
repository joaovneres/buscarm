package com.buscarm.registro.adapter.in.web;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.buscarm.registro.adapter.in.web.dto.RegistroRequest;
import com.buscarm.registro.adapter.in.web.dto.RegistroResponse;
import com.buscarm.registro.adapter.in.web.dto.RegistroWebMapper;
import com.buscarm.registro.application.port.in.AlterarRegistroUseCase;
import com.buscarm.registro.application.port.in.AlterarRegistroUseCase.AlterarRegistroCommand;
import com.buscarm.registro.application.port.in.BuscarRegistrosUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase.CadastrarRegistroCommand;
import com.buscarm.registro.application.port.in.ExportarRegistrosUseCase;
import com.buscarm.registro.domain.FormatoExportacao;
import com.buscarm.shared.ArquivoExportado;
import com.buscarm.shared.Pagina;

/** Adaptador de entrada (driving): expõe os casos de uso via HTTP. */
@RestController
@RequestMapping("/api/registros")
public class RegistroController {

    private final CadastrarRegistroUseCase cadastrarRegistro;
    private final BuscarRegistrosUseCase buscarRegistros;
    private final ExportarRegistrosUseCase exportarRegistros;
    private final AlterarRegistroUseCase alterarRegistro;

    public RegistroController(CadastrarRegistroUseCase cadastrarRegistro,
                             BuscarRegistrosUseCase buscarRegistros,
                             ExportarRegistrosUseCase exportarRegistros,
                             AlterarRegistroUseCase alterarRegistro) {
        this.cadastrarRegistro = cadastrarRegistro;
        this.buscarRegistros = buscarRegistros;
        this.exportarRegistros = exportarRegistros;
        this.alterarRegistro = alterarRegistro;
    }

    @PostMapping
    public ResponseEntity<RegistroResponse> criar(@Valid @RequestBody RegistroRequest req) {
        var salvo = cadastrarRegistro.cadastrar(
                new CadastrarRegistroCommand(req.rm(), req.nomeCompleto(), req.anoRegistro()));
        return ResponseEntity.status(HttpStatus.CREATED).body(RegistroWebMapper.paraResponse(salvo));
    }

    @PutMapping("/{id}")
    public RegistroResponse alterar(@PathVariable long id, @Valid @RequestBody RegistroRequest req) {
        var alterado = alterarRegistro.alterar(id,
                new AlterarRegistroCommand(req.rm(), req.nomeCompleto(), req.anoRegistro()));
        return RegistroWebMapper.paraResponse(alterado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable long id) {
        alterarRegistro.desativar(id);
    }

    @GetMapping("/{id}")
    public RegistroResponse buscarPorId(@PathVariable long id) {
        return RegistroWebMapper.paraResponse(buscarRegistros.buscarPorId(id));
    }

    /** Exporta todos os registros ativos (.xlsx por padrão, ou ?formato=csv). */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam(defaultValue = "xlsx") String formato) {
        FormatoExportacao fmt = "csv".equalsIgnoreCase(formato)
                ? FormatoExportacao.CSV
                : FormatoExportacao.XLSX;
        ArquivoExportado arquivo = exportarRegistros.exportar(fmt);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + arquivo.nomeArquivo() + "\"")
                .contentType(MediaType.parseMediaType(arquivo.contentType()))
                .body(arquivo.conteudo());
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
