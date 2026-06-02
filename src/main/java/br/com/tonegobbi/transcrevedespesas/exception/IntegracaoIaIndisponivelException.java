package br.com.tonegobbi.transcrevedespesas.exception;

public class IntegracaoIaIndisponivelException extends RuntimeException {

    public IntegracaoIaIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegracaoIaIndisponivelException(String message) {
        super(message);
    }
}
