package br.com.tonegobbi.transcrevedespesas.dto;

import jakarta.validation.constraints.NotBlank;

public record SinteseRequest(@NotBlank String texto) {
}
