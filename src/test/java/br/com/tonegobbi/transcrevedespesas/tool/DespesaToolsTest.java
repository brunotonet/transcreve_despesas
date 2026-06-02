package br.com.tonegobbi.transcrevedespesas.tool;

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

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DespesaToolsTest {

    @Mock
    private DespesaService service;

    @InjectMocks
    private DespesaTools tools;

    @Test
    void registraDespesaInterpretadaPelaIa() {
        AiRequestContext.definir(OrigemLancamento.TEXTO, "Gastei 45 reais com gasolina ontem");
        try {
            tools.registrarDespesa("Gasolina", new BigDecimal("45.00"), CategoriaDespesa.COMBUSTIVEL,
                    "2026-05-31", null, null);
        } finally {
            AiRequestContext.limpar();
        }

        verify(service).registrarPorIa("Gasolina", new BigDecimal("45.00"), CategoriaDespesa.COMBUSTIVEL,
                LocalDate.of(2026, 5, 31), null, null, "Gastei 45 reais com gasolina ontem",
                OrigemLancamento.TEXTO);
    }
}
