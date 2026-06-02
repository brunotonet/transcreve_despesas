package br.com.tonegobbi.transcrevedespesas.dto;

import jakarta.validation.constraints.NotBlank;

public record AssistenteTextoRequest(String conversationId, @NotBlank String mensagem) {
}
