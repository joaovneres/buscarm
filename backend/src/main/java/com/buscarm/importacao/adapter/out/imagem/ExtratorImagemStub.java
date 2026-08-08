package com.buscarm.importacao.adapter.out.imagem;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.buscarm.importacao.application.port.out.ExtratorImagemPort;
import com.buscarm.importacao.domain.ExtratorLinhasOcr;
import com.buscarm.importacao.domain.RegistroExtraido;

/**
 * Substituto do OCR nos testes (não há Tesseract nativo no CI/H2). Interpreta o conteúdo enviado
 * como o texto que o OCR reconheceria, exercitando o mesmo parser de linhas do adaptador real.
 */
@Component
@Profile("test")
public class ExtratorImagemStub implements ExtratorImagemPort {

    @Override
    public List<RegistroExtraido> extrair(byte[] imagem) {
        return ExtratorLinhasOcr.extrair(new String(imagem, StandardCharsets.UTF_8));
    }
}
