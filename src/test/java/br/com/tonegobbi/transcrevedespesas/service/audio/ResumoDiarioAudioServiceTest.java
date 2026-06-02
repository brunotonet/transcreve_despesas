package br.com.tonegobbi.transcrevedespesas.service.audio;

import br.com.tonegobbi.transcrevedespesas.dto.DespesaResponse;
import br.com.tonegobbi.transcrevedespesas.enums.CategoriaDespesa;
import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;
import br.com.tonegobbi.transcrevedespesas.service.DespesaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ResumoDiarioAudioServiceTest {

    @Mock
    private DespesaService despesaService;

    @Mock
    private SinteseAudioService sinteseAudioService;

    @InjectMocks
    private ResumoDiarioAudioService service;

    @Test
    void sintetizaTotalEListaCadaDespesaDoDia() {
        var data = LocalDate.of(2026, 6, 1);
        var despesas = List.of(
                despesa("Oficina", "500.00", CategoriaDespesa.OUTROS, data),
                despesa("Mercado", "300.00", CategoriaDespesa.MERCADO, data),
                despesa("Gasolina", "200.00", CategoriaDespesa.COMBUSTIVEL, data));
        var texto = "O total de gastos deste dia foi 1000 reais. "
                + "Voce gastou 500 reais com Oficina, categoria outros. "
                + "Voce gastou 300 reais com Mercado, categoria mercado. "
                + "Voce gastou 200 reais com Gasolina, categoria combustivel.";
        var audio = new byte[]{4, 5, 6};
        when(despesaService.listarPorPeriodo(data, data)).thenReturn(despesas);
        when(sinteseAudioService.sintetizar(texto)).thenReturn(audio);

        assertThat(service.sintetizarResumo(data)).isEqualTo(audio);
        verify(sinteseAudioService).sintetizar(texto);
    }

    @Test
    void sintetizaMensagemQuandoNaoExistemDespesasNoDia() {
        var data = LocalDate.of(2026, 6, 2);
        var texto = "Nao foram registradas despesas neste dia.";
        var audio = new byte[]{7, 8, 9};
        when(despesaService.listarPorPeriodo(data, data)).thenReturn(List.of());
        when(sinteseAudioService.sintetizar(texto)).thenReturn(audio);

        assertThat(service.sintetizarResumo(data)).isEqualTo(audio);
        verify(sinteseAudioService).sintetizar(texto);
    }

    private DespesaResponse despesa(String descricao, String valor, CategoriaDespesa categoria, LocalDate data) {
        return new DespesaResponse(null, descricao, new BigDecimal(valor), categoria, data,
                null, null, OrigemLancamento.MANUAL, null, null, null);
    }
}
