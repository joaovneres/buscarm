package com.buscarm.importacao.adapter.out.imagem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.buscarm.importacao.application.port.out.ExtratorImagemPort;
import com.buscarm.importacao.domain.ExtratorLinhasOcr;
import com.buscarm.importacao.domain.RegistroExtraido;

/**
 * Adaptador de OCR que invoca o binário nativo do Tesseract (instalado na imagem — ver Dockerfile).
 * Fica fora do profile de teste — nos testes usa-se {@link ExtratorImagemStub}.
 *
 * <p>Chamamos o executável {@code tesseract} diretamente (em vez da biblioteca Tess4J) por dois
 * motivos observados nesta base: a Tess4J degradava o reconhecimento, e o modo de segmentação
 * precisa ser <b>automático</b> (--psm 3) — o livro tem duas colunas (Nome | RM) e o modo de bloco
 * único junta as colunas e perde o RM. Usamos o engine LSTM (--oem 1), único presente no pacote de
 * idioma {@code por} do Debian.
 */
@Component
@Profile("!test")
public class TesseractCliExtratorAdapter implements ExtratorImagemPort {

    private static final long TIMEOUT_SEGUNDOS = 60;

    private final String binario;
    private final String datapath;
    private final String idioma;

    public TesseractCliExtratorAdapter(
            @Value("${buscarm.ocr.binario:tesseract}") String binario,
            @Value("${buscarm.ocr.datapath:/usr/share/tesseract-ocr/5/tessdata}") String datapath,
            @Value("${buscarm.ocr.idioma:por}") String idioma) {
        this.binario = binario;
        this.datapath = datapath;
        this.idioma = idioma;
    }

    @Override
    public List<RegistroExtraido> extrair(byte[] imagem) {
        String extensao = detectarExtensao(imagem);
        File temp = null;
        try {
            temp = File.createTempFile("buscarm-ocr-", "." + extensao);
            Files.write(temp.toPath(), imagem);
            return ExtratorLinhasOcr.extrair(rodarTesseract(temp));
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível processar a imagem: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OCR interrompido", e);
        } finally {
            if (temp != null && !temp.delete()) {
                temp.deleteOnExit();
            }
        }
    }

    private String rodarTesseract(File imagem) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                binario, imagem.getAbsolutePath(), "stdout",
                "--tessdata-dir", datapath,
                "-l", idioma,
                "--oem", "1",
                "--psm", "3");
        pb.redirectErrorStream(false);
        Process processo = pb.start();
        // Lê stdout/stderr antes do waitFor para não travar em buffers cheios.
        byte[] saida = processo.getInputStream().readAllBytes();
        String erro = new String(processo.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!processo.waitFor(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)) {
            processo.destroyForcibly();
            throw new IllegalStateException("OCR excedeu o tempo limite");
        }
        if (processo.exitValue() != 0) {
            throw new IllegalStateException("Falha no OCR (tesseract): " + erro.strip());
        }
        return new String(saida, StandardCharsets.UTF_8);
    }

    /** Valida que os bytes são uma imagem legível e devolve a extensão do formato (jpg, png…). */
    private static String detectarExtensao(byte[] imagem) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imagem))) {
            if (iis != null) {
                Iterator<ImageReader> leitores = ImageIO.getImageReaders(iis);
                if (leitores.hasNext()) {
                    String formato = leitores.next().getFormatName().toLowerCase(Locale.ROOT);
                    return "jpeg".equals(formato) ? "jpg" : formato;
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler a imagem: " + e.getMessage());
        }
        throw new IllegalArgumentException("Arquivo de imagem inválido");
    }
}
