package br.com.tonegobbi.transcrevedespesas.service;

import br.com.tonegobbi.transcrevedespesas.entity.InteracaoIa;
import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;
import br.com.tonegobbi.transcrevedespesas.repository.InteracaoIaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteracaoIaServiceTest {

    @Mock
    private InteracaoIaRepository repository;

    @InjectMocks
    private InteracaoIaService service;

    @Test
    void formataContextoPersistidoEmOrdemCronologica() {
        when(repository.findTop10ByConversationIdOrderByCreatedAtDesc("conv-1")).thenReturn(List.of(
                new InteracaoIa("conv-1", OrigemLancamento.TEXTO, "Quanto gastei?", null, "R$ 45"),
                new InteracaoIa("conv-1", OrigemLancamento.TEXTO, "Gastei 45", null, "Registrado")
        ));

        var contexto = service.contextoRecente("conv-1");

        assertThat(contexto).containsSubsequence("Usuario: Gastei 45", "Assistente: Registrado",
                "Usuario: Quanto gastei?", "Assistente: R$ 45");
    }
}
