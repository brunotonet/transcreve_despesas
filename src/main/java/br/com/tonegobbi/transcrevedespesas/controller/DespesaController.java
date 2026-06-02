package br.com.tonegobbi.transcrevedespesas.controller;

import br.com.tonegobbi.transcrevedespesas.dto.DespesaRequest;
import br.com.tonegobbi.transcrevedespesas.dto.DespesaResponse;
import br.com.tonegobbi.transcrevedespesas.dto.TotalCategoriaResponse;
import br.com.tonegobbi.transcrevedespesas.dto.TotalResponse;
import br.com.tonegobbi.transcrevedespesas.enums.CategoriaDespesa;
import br.com.tonegobbi.transcrevedespesas.service.DespesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/despesas")
@Tag(name = "Despesas", description = "Registro manual e consultas financeiras")
public class DespesaController {

    private final DespesaService service;

    public DespesaController(DespesaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar despesa manual")
    public DespesaResponse registrar(@Valid @RequestBody DespesaRequest request) {
        return service.registrarManual(request);
    }

    @GetMapping
    @Operation(summary = "Listar despesas")
    public List<DespesaResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar despesa por ID")
    public DespesaResponse buscar(@PathVariable Long id) { return service.buscarPorId(id); }

    @GetMapping("/periodo")
    @Operation(summary = "Listar despesas por periodo")
    public List<DespesaResponse> periodo(@RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return service.listarPorPeriodo(inicio, fim);
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Listar despesas por categoria")
    public List<DespesaResponse> categoria(@PathVariable CategoriaDespesa categoria) {
        return service.listarPorCategoria(categoria);
    }

    @GetMapping("/totais/periodo")
    @Operation(summary = "Consultar total por periodo")
    public TotalResponse totalPeriodo(@RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return service.totalPorPeriodo(inicio, fim);
    }

    @GetMapping("/totais/categoria")
    @Operation(summary = "Consultar totais agrupados por categoria")
    public List<TotalCategoriaResponse> totaisCategoria() { return service.totalPorCategoria(); }
}
