package br.com.semblefe.compartilhado.web;

import java.time.Instant;
import java.util.List;

public record ErroApi(
        Instant timestamp,
        int status,
        String codigo,
        String mensagem,
        String caminho,
        String requestId,
        List<CampoInvalido> campos) {

    public record CampoInvalido(String campo, String mensagem) {
    }
}
