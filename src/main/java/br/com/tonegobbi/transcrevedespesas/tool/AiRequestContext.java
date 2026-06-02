package br.com.tonegobbi.transcrevedespesas.tool;

import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;

public final class AiRequestContext {

    private static final ThreadLocal<Contexto> CONTEXTO = new ThreadLocal<>();

    private AiRequestContext() {
    }

    public static void definir(OrigemLancamento origem, String textoOriginal) {
        CONTEXTO.set(new Contexto(origem, textoOriginal));
    }

    public static Contexto atual() {
        var contexto = CONTEXTO.get();
        return contexto != null ? contexto : new Contexto(OrigemLancamento.IA, null);
    }

    public static void limpar() {
        CONTEXTO.remove();
    }

    public record Contexto(OrigemLancamento origem, String textoOriginal) {
    }
}
