package com.buscarm.importacao.adapter.out.imagem;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.buscarm.importacao.application.port.out.ExtratorImagemPort;
import com.buscarm.importacao.domain.ExtratorLinhasOcr;
import com.buscarm.importacao.domain.RegistroExtraido;

/**
 * Adaptador de OCR com Tesseract (Tess4J). Requer o Tesseract nativo instalado (ver Dockerfile).
 * Fica fora do profile de teste — nos testes usa-se {@link ExtratorImagemStub}.
 */
@Component
@Profile("!test")
public class Tess4jExtratorAdapter implements ExtratorImagemPort {

    private final String datapath;
    private final String idioma;

    public Tess4jExtratorAdapter(
            @Value("${buscarm.ocr.datapath:/usr/share/tesseract-ocr/5/tessdata}") String datapath,
            @Value("${buscarm.ocr.idioma:por}") String idioma) {
        this.datapath = datapath;
        this.idioma = idioma;
    }

    @Override
    public List<RegistroExtraido> extrair(byte[] imagem) {
        BufferedImage img;
        try {
            img = ImageIO.read(new ByteArrayInputStream(imagem));
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler a imagem: " + e.getMessage());
        }
        if (img == null) {
            throw new IllegalArgumentException("Arquivo de imagem inválido");
        }
        // Tesseract não é thread-safe: uma instância por chamada.
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(datapath);
        tesseract.setLanguage(idioma);
        try {
            return ExtratorLinhasOcr.extrair(tesseract.doOCR(img));
        } catch (TesseractException e) {
            throw new IllegalStateException("Falha no OCR: " + e.getMessage(), e);
        }
    }
}
