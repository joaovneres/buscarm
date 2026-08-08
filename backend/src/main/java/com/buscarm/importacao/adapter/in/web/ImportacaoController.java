package com.buscarm.importacao.adapter.in.web;

import java.io.IOException;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.buscarm.importacao.adapter.in.web.dto.ConfirmarImagemRequest;
import com.buscarm.importacao.adapter.in.web.dto.ConfirmarRequest;
import com.buscarm.importacao.adapter.in.web.dto.ImportacaoResponse;
import com.buscarm.importacao.adapter.in.web.dto.ImportacaoWebMapper;
import com.buscarm.importacao.application.port.in.ConfirmarImagemUseCase;
import com.buscarm.importacao.application.port.in.ConfirmarImagemUseCase.ItemCorrigido;
import com.buscarm.importacao.application.port.in.ConfirmarImportacaoUseCase;
import com.buscarm.importacao.application.port.in.ConsultarImportacaoUseCase;
import com.buscarm.importacao.application.port.in.ProcessarImagemUseCase;
import com.buscarm.importacao.application.port.in.ValidarPlanilhaUseCase;

/** Adaptador de entrada (web) do pipeline de importação por planilha. */
@RestController
@RequestMapping("/api/importacoes")
public class ImportacaoController {

    private final ValidarPlanilhaUseCase validarPlanilha;
    private final ConfirmarImportacaoUseCase confirmarImportacao;
    private final ConsultarImportacaoUseCase consultarImportacao;
    private final ProcessarImagemUseCase processarImagem;
    private final ConfirmarImagemUseCase confirmarImagem;

    public ImportacaoController(ValidarPlanilhaUseCase validarPlanilha,
                                ConfirmarImportacaoUseCase confirmarImportacao,
                                ConsultarImportacaoUseCase consultarImportacao,
                                ProcessarImagemUseCase processarImagem,
                                ConfirmarImagemUseCase confirmarImagem) {
        this.validarPlanilha = validarPlanilha;
        this.confirmarImportacao = confirmarImportacao;
        this.consultarImportacao = consultarImportacao;
        this.processarImagem = processarImagem;
        this.confirmarImagem = confirmarImagem;
    }

    /** Pré-visualização: NÃO grava no cadastro. */
    @PostMapping(value = "/planilha/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacaoResponse validar(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo da planilha é obrigatório");
        }
        String nome = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "planilha.xlsx";
        var importacao = validarPlanilha.validar(nome, arquivo.getBytes());
        return ImportacaoWebMapper.paraResponse(importacao);
    }

    /** Efetiva apenas os itens válidos aprovados. */
    @PostMapping("/planilha/confirmar")
    public ImportacaoResponse confirmar(@Valid @RequestBody ConfirmarRequest req) {
        List<Integer> linhas = req.linhasAprovadas() != null ? req.linhasAprovadas() : List.of();
        return ImportacaoWebMapper.paraResponse(
                confirmarImportacao.confirmar(req.importacaoId(), linhas));
    }

    /** OCR: processa a foto e devolve a conferência (NÃO grava). */
    @PostMapping(value = "/imagem/processar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacaoResponse processarImagem(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Imagem é obrigatória");
        }
        String nome = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "foto.jpg";
        return ImportacaoWebMapper.paraResponse(processarImagem.processar(nome, arquivo.getBytes()));
    }

    /** OCR: efetiva os itens revisados/aprovados na conferência. */
    @PostMapping("/imagem/confirmar")
    public ImportacaoResponse confirmarImagem(@Valid @RequestBody ConfirmarImagemRequest req) {
        List<ItemCorrigido> itens = (req.itens() != null ? req.itens() : List.<ConfirmarImagemRequest.ItemCorrigidoRequest>of())
                .stream()
                .map(i -> new ItemCorrigido(i.linha(), i.rm(), i.nomeCompleto()))
                .toList();
        return ImportacaoWebMapper.paraResponse(confirmarImagem.confirmarImagem(req.importacaoId(), itens));
    }

    @GetMapping("/{id}")
    public ImportacaoResponse consultar(@PathVariable long id) {
        return ImportacaoWebMapper.paraResponse(consultarImportacao.buscarPorId(id));
    }
}
