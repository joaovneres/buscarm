package com.buscarm.registro;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.comum.NomeNormalizer;
import com.buscarm.comum.PaginaResponse;
import com.buscarm.comum.RecursoNaoEncontradoException;
import com.buscarm.comum.RegraNegocioException;
import com.buscarm.registro.dto.RegistroMapper;
import com.buscarm.registro.dto.RegistroRequest;
import com.buscarm.registro.dto.RegistroResponse;
import com.buscarm.seguranca.UsuarioAtualProvider;
import com.buscarm.seguranca.UsuarioSessaoBanco;

@Service
public class RegistroService {

    private final RegistroRepository repository;
    private final UsuarioAtualProvider usuarioAtual;
    private final UsuarioSessaoBanco usuarioSessaoBanco;

    public RegistroService(RegistroRepository repository,
                           UsuarioAtualProvider usuarioAtual,
                           UsuarioSessaoBanco usuarioSessaoBanco) {
        this.repository = repository;
        this.usuarioAtual = usuarioAtual;
        this.usuarioSessaoBanco = usuarioSessaoBanco;
    }

    @Transactional
    public RegistroResponse criar(RegistroRequest req) {
        usuarioSessaoBanco.definir(usuarioAtual.getUsuario());
        if (repository.existsByRm(req.rm())) {
            throw new RegraNegocioException("RM " + req.rm() + " já cadastrado");
        }
        Registro r = new Registro();
        r.setRm(req.rm());
        r.setNomeCompleto(req.nomeCompleto().trim());
        r.setSobrenome(NomeNormalizer.extrairSobrenome(req.nomeCompleto()));
        r.setNomeBusca(NomeNormalizer.normalizar(req.nomeCompleto()));
        r.setAnoRegistro(req.anoRegistro());
        r.setAtivo(true);
        return RegistroMapper.paraResponse(repository.save(r));
    }

    @Transactional(readOnly = true)
    public RegistroResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(RegistroMapper::paraResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<RegistroResponse> pesquisar(String nome, Integer rm, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("sobrenome").ascending());
        Page<Registro> resultado;
        if (rm != null) {
            List<Registro> encontrados = repository.findByRm(rm)
                    .filter(Registro::isAtivo)
                    .map(List::of)
                    .orElseGet(List::of);
            resultado = new PageImpl<>(encontrados, pageable, encontrados.size());
        } else if (nome != null && !nome.isBlank()) {
            resultado = repository.findByAtivoTrueAndNomeBuscaContaining(
                    NomeNormalizer.normalizar(nome), pageable);
        } else {
            resultado = repository.findByAtivoTrue(pageable);
        }
        return PaginaResponse.de(resultado.map(RegistroMapper::paraResponse));
    }
}
