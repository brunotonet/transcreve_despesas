package br.com.tonegobbi.transcrevedespesas.dto;

public record LoginResponse(
        String token,
        String type,
        long expiresInMinutes
) {
}
