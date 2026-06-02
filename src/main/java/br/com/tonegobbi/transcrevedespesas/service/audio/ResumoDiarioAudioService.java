package br.com.tonegobbi.transcrevedespesas.service.audio;

import br.com.tonegobbi.transcrevedespesas.dto.DespesaResponse;
import br.com.tonegobbi.transcrevedespesas.service.DespesaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class ResumoDiarioAudioService {

    private static final String SEM_DESPESAS = "Nao foram registradas despesas neste dia.";

    private final DespesaService despesaService;
    private final SinteseAudioService sinteseAudioService;

    public ResumoDiarioAudioService(DespesaService despesaService, SinteseAudioService sinteseAudioService) {
        this.despesaService = despesaService;
        this.sinteseAudioService = sinteseAudioService;
    }

    public byte[] sintetizarResumo(LocalDate data) {
        var despesas = despesaService.listarPorPeriodo(data, data);
        return sinteseAudioService.sintetizar(montarTexto(despesas));
    }

    private String montarTexto(List<DespesaResponse> despesas) {
        if (despesas.isEmpty()) {
            return SEM_DESPESAS;
        }

        var total = despesas.stream()
                .map(DespesaResponse::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var texto = new StringBuilder("O total de gastos deste dia foi ")
                .append(formatarValor(total))
                .append(" reais. ");

        despesas.forEach(despesa -> texto.append("Voce gastou ")
                .append(formatarValor(despesa.valor()))
                .append(" reais com ")
                .append(despesa.descricao())
                .append(", categoria ")
                .append(despesa.categoria().name().toLowerCase(Locale.ROOT))
                .append(". "));

        return texto.toString().trim();
    }

    private String formatarValor(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }
}
