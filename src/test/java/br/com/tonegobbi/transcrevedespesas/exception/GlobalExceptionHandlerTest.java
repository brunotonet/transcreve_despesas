package br.com.tonegobbi.transcrevedespesas.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void retornaServiceUnavailableQuandoIntegracaoIaEstaIndisponivel() {
        var exception = new IntegracaoIaIndisponivelException("Servico temporariamente indisponivel");

        var problemDetail = new GlobalExceptionHandler().integracaoIaIndisponivel(exception);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(problemDetail.getDetail()).isEqualTo("Servico temporariamente indisponivel");
    }
}
