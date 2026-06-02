package br.com.tonegobbi.transcrevedespesas.tool;

import br.com.tonegobbi.transcrevedespesas.dto.DespesaResponse;
import br.com.tonegobbi.transcrevedespesas.dto.TotalCategoriaResponse;
import br.com.tonegobbi.transcrevedespesas.dto.TotalResponse;
import br.com.tonegobbi.transcrevedespesas.enums.CategoriaDespesa;
import br.com.tonegobbi.transcrevedespesas.service.DespesaService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DespesaTools {

    private final DespesaService service;

    public DespesaTools(DespesaService service) {
        this.service = service;
    }

    @Tool(description = "Registra uma despesa interpretada da mensagem do usuario")
    public DespesaResponse registrarDespesa(
            @ToolParam(description = "Descricao curta da despesa") String descricao,
            @ToolParam(description = "Valor positivo em reais") BigDecimal valor,
            @ToolParam(description = "Categoria da despesa") CategoriaDespesa categoria,
            @ToolParam(description = "Data da despesa no formato ISO yyyy-MM-dd") String dataDespesa,
            @ToolParam(description = "Forma de pagamento quando informada", required = false) String formaPagamento,
            @ToolParam(description = "Observacao adicional quando informada", required = false) String observacao) {
        var contexto = AiRequestContext.atual();
        return service.registrarPorIa(descricao, valor, categoria, dataIso(dataDespesa), formaPagamento, observacao,
                contexto.textoOriginal(), contexto.origem());
    }

    @Tool(description = "Lista despesas de um periodo inclusivo")
    public List<DespesaResponse> listarDespesasPorPeriodo(String inicio, String fim) {
        return service.listarPorPeriodo(dataIso(inicio), dataIso(fim));
    }

    @Tool(description = "Lista despesas de uma categoria")
    public List<DespesaResponse> listarDespesasPorCategoria(CategoriaDespesa categoria) {
        return service.listarPorCategoria(categoria);
    }

    @Tool(description = "Consulta o total gasto em um periodo inclusivo")
    public TotalResponse consultarTotalPorPeriodo(String inicio, String fim) {
        return service.totalPorPeriodo(dataIso(inicio), dataIso(fim));
    }

    @Tool(description = "Consulta os totais agrupados por categoria")
    public List<TotalCategoriaResponse> consultarTotaisPorCategoria() {
        return service.totalPorCategoria();
    }

    @Tool(description = "Consulta a maior despesa registrada")
    public DespesaResponse consultarMaiorDespesa() {
        return service.maiorDespesa();
    }

    private LocalDate dataIso(String data) {
        return LocalDate.parse(data);
    }
}
