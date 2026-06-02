package br.com.tonegobbi.transcrevedespesas.entity;

import br.com.tonegobbi.transcrevedespesas.enums.CategoriaDespesa;
import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "despesas")
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaDespesa categoria;

    @Column(name = "data_despesa", nullable = false)
    private LocalDate dataDespesa;

    @Column(name = "forma_pagamento", length = 60)
    private String formaPagamento;

    @Column(length = 500)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_lancamento", nullable = false, length = 20)
    private OrigemLancamento origemLancamento;

    @Column(name = "texto_original", length = 2000)
    private String textoOriginal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Despesa() {
    }

    public Despesa(String descricao, BigDecimal valor, CategoriaDespesa categoria, LocalDate dataDespesa,
                   String formaPagamento, String observacao, OrigemLancamento origemLancamento,
                   String textoOriginal) {
        this.descricao = descricao;
        this.valor = valor;
        this.categoria = categoria;
        this.dataDespesa = dataDespesa;
        this.formaPagamento = formaPagamento;
        this.observacao = observacao;
        this.origemLancamento = origemLancamento;
        this.textoOriginal = textoOriginal;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public CategoriaDespesa getCategoria() { return categoria; }
    public LocalDate getDataDespesa() { return dataDespesa; }
    public String getFormaPagamento() { return formaPagamento; }
    public String getObservacao() { return observacao; }
    public OrigemLancamento getOrigemLancamento() { return origemLancamento; }
    public String getTextoOriginal() { return textoOriginal; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
