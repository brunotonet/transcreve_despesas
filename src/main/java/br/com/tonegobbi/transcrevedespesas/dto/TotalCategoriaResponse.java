package br.com.tonegobbi.transcrevedespesas.dto;

import br.com.tonegobbi.transcrevedespesas.enums.CategoriaDespesa;

import java.math.BigDecimal;

public record TotalCategoriaResponse(CategoriaDespesa categoria, BigDecimal total) {
}
