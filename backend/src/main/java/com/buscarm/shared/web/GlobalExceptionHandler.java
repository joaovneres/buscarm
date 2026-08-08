package com.buscarm.shared.web;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.buscarm.registro.domain.exception.RegistroDuplicadoException;
import com.buscarm.registro.domain.exception.RegistroNaoEncontradoException;
import com.buscarm.shared.ApiError;

/** Traduz exceções de domínio/validação em respostas {@link ApiError} consistentes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacao(MethodArgumentNotValidException ex,
                                                    HttpServletRequest req) {
        List<ApiError.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.CampoErro(f.getField(), f.getDefaultMessage()))
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "VALIDACAO", "Dados inválidos", req, campos);
    }

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleNaoEncontrado(RegistroNaoEncontradoException ex,
                                                        HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(RegistroDuplicadoException.class)
    public ResponseEntity<ApiError> handleDuplicado(RegistroDuplicadoException ex,
                                                    HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, "REGRA_NEGOCIO", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenerico(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Erro interno inesperado", req, List.of());
    }

    private ResponseEntity<ApiError> construir(HttpStatus status, String code, String message,
                                               HttpServletRequest req,
                                               List<ApiError.CampoErro> campos) {
        ApiError erro = new ApiError(Instant.now(), status.value(), code, message,
                req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(erro);
    }
}
