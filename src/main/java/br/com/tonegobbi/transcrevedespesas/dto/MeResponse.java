package br.com.tonegobbi.transcrevedespesas.dto;

import java.util.List;

public record MeResponse(
        String username,
        List<String> authorities
) {
}
