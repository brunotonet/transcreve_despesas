package br.com.tonegobbi.transcrevedespesas.service;

import br.com.tonegobbi.transcrevedespesas.dto.DespesaRequest;
import br.com.tonegobbi.transcrevedespesas.entity.Despesa;
import br.com.tonegobbi.transcrevedespesas.enums.CategoriaDespesa;
import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;
import br.com.tonegobbi.transcrevedespesas.repository.DespesaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {

    @Mock
    private DespesaRepository repository;

    @InjectMocks
    private DespesaService service;

    @Test
    void registraDespesaManualComOrigemManual() {
        var request = new DespesaRequest("Mercado", new BigDecimal("89.90"), CategoriaDespesa.MERCADO,
                LocalDate.of(2026, 6, 1), "PIX", null);
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.registrarManual(request);

        var captor = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOrigemLancamento()).isEqualTo(OrigemLancamento.MANUAL);
        assertThat(captor.getValue().getValor()).isEqualByComparingTo("89.90");
    }

    @Test
    void consultaTotalDoPeriodo() {
        var inicio = LocalDate.of(2026, 6, 1);
        var fim = LocalDate.of(2026, 6, 30);
        when(repository.totalPorPeriodo(inicio, fim)).thenReturn(new BigDecimal("134.90"));

        assertThat(service.totalPorPeriodo(inicio, fim).total()).isEqualByComparingTo("134.90");
    }

    @Test
    void listaDespesasPorCategoria() {
        when(repository.findByCategoriaOrderByDataDespesaDescIdDesc(CategoriaDespesa.ALIMENTACAO))
                .thenReturn(List.of());

        assertThat(service.listarPorCategoria(CategoriaDespesa.ALIMENTACAO)).isEmpty();
    }
}
