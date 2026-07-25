package br.com.semblefe.compartilhado.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class TratadorGlobalErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroApi> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<ErroApi.CampoInvalido> campos = exception.getBindingResult().getFieldErrors().stream()
                .map(this::campoInvalido)
                .toList();

        ErroApi erro = novoErro(
                HttpStatus.BAD_REQUEST,
                "REQUISICAO_INVALIDA",
                "Verifique os campos informados.",
                request,
                campos);

        return ResponseEntity.badRequest().body(erro);
    }

    private ErroApi.CampoInvalido campoInvalido(FieldError erro) {
        return new ErroApi.CampoInvalido(erro.getField(), erro.getDefaultMessage());
    }

    private ErroApi novoErro(
            HttpStatus status,
            String codigo,
            String mensagem,
            HttpServletRequest request,
            List<ErroApi.CampoInvalido> campos) {

        return new ErroApi(
                Instant.now(),
                status.value(),
                codigo,
                mensagem,
                request.getRequestURI(),
                MDC.get("requestId"),
                campos);
    }
}
