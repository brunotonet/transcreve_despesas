package br.com.tonegobbi.transcrevedespesas.dto;

import br.com.tonegobbi.transcrevedespesas.entity.InteracaoIa;
import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;

import java.time.Instant;

public record InteracaoIaResponse(Long id, String conversationId, OrigemLancamento origem, String textoOriginal,
                                  String transcricao, String resposta, Instant createdAt) {
    public static InteracaoIaResponse from(InteracaoIa interacao) {
        return new InteracaoIaResponse(interacao.getId(), interacao.getConversationId(), interacao.getOrigem(),
                interacao.getTextoOriginal(), interacao.getTranscricao(), interacao.getResposta(),
                interacao.getCreatedAt());
    }
}
