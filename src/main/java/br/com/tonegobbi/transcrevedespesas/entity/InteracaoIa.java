package br.com.tonegobbi.transcrevedespesas.entity;

import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "interacoes_ia")
public class InteracaoIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, length = 100)
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemLancamento origem;

    @Column(name = "texto_original", nullable = false, length = 4000)
    private String textoOriginal;

    @Column(length = 4000)
    private String transcricao;

    @Column(nullable = false, length = 8000)
    private String resposta;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected InteracaoIa() {
    }

    public InteracaoIa(String conversationId, OrigemLancamento origem, String textoOriginal,
                       String transcricao, String resposta) {
        this.conversationId = conversationId;
        this.origem = origem;
        this.textoOriginal = textoOriginal;
        this.transcricao = transcricao;
        this.resposta = resposta;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getConversationId() { return conversationId; }
    public OrigemLancamento getOrigem() { return origem; }
    public String getTextoOriginal() { return textoOriginal; }
    public String getTranscricao() { return transcricao; }
    public String getResposta() { return resposta; }
    public Instant getCreatedAt() { return createdAt; }
}
