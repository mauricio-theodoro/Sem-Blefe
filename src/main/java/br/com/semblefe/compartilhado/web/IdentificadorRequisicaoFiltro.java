package br.com.semblefe.compartilhado.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class IdentificadorRequisicaoFiltro extends OncePerRequestFilter {

    public static final String CABECALHO = "X-Request-Id";
    private static final String CHAVE_MDC = "requestId";
    private static final Pattern VALOR_SEGURO = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String recebido = request.getHeader(CABECALHO);
        String requestId = recebido != null && VALOR_SEGURO.matcher(recebido).matches()
                ? recebido
                : UUID.randomUUID().toString();

        MDC.put(CHAVE_MDC, requestId);
        response.setHeader(CABECALHO, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CHAVE_MDC);
        }
    }
}
