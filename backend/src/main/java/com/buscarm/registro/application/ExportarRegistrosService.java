package com.buscarm.registro.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.registro.application.port.in.ExportarRegistrosUseCase;
import com.buscarm.registro.application.port.out.GeradorArquivoRegistrosPort;
import com.buscarm.registro.application.port.out.RegistroRepositoryPort;
import com.buscarm.registro.domain.FormatoExportacao;
import com.buscarm.registro.domain.Registro;
import com.buscarm.shared.ArquivoExportado;

/** Exporta todos os registros ativos delegando a geração do arquivo a um adaptador. */
@Service
public class ExportarRegistrosService implements ExportarRegistrosUseCase {

    private final RegistroRepositoryPort repositorio;
    private final GeradorArquivoRegistrosPort gerador;

    public ExportarRegistrosService(RegistroRepositoryPort repositorio,
                                    GeradorArquivoRegistrosPort gerador) {
        this.repositorio = repositorio;
        this.gerador = gerador;
    }

    @Override
    @Transactional(readOnly = true)
    public ArquivoExportado exportar(FormatoExportacao formato) {
        List<Registro> registros = repositorio.listarTodosAtivos();
        return gerador.gerar(registros, formato);
    }
}
