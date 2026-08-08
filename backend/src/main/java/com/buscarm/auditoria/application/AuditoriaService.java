package com.buscarm.auditoria.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.auditoria.application.port.in.ConsultarAuditoriaUseCase;
import com.buscarm.auditoria.application.port.in.RegistrarAuditoriaUseCase;
import com.buscarm.auditoria.application.port.out.AuditoriaRepositoryPort;
import com.buscarm.auditoria.domain.RegistroAuditoria;
import com.buscarm.shared.Pagina;
import com.buscarm.shared.UsuarioAtualPort;

@Service
public class AuditoriaService implements RegistrarAuditoriaUseCase, ConsultarAuditoriaUseCase {

    private final AuditoriaRepositoryPort repositorio;
    private final UsuarioAtualPort usuarioAtual;

    public AuditoriaService(AuditoriaRepositoryPort repositorio, UsuarioAtualPort usuarioAtual) {
        this.repositorio = repositorio;
        this.usuarioAtual = usuarioAtual;
    }

    @Override
    @Transactional
    public void registrar(String acao, String entidade, Long entidadeId, String detalhe) {
        repositorio.salvar(RegistroAuditoria.novo(
                usuarioAtual.getUsuarioAtual(), acao, entidade, entidadeId, detalhe));
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<RegistroAuditoria> listar(int pagina, int tamanho) {
        return repositorio.listar(pagina, tamanho);
    }
}
