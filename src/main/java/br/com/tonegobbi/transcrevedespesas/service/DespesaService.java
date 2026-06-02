package br.com.tonegobbi.transcrevedespesas.service;

import br.com.tonegobbi.transcrevedespesas.dto.DespesaRequest;
import br.com.tonegobbi.transcrevedespesas.dto.DespesaResponse;
import br.com.tonegobbi.transcrevedespesas.dto.TotalCategoriaResponse;
import br.com.tonegobbi.transcrevedespesas.dto.TotalResponse;
import br.com.tonegobbi.transcrevedespesas.entity.Despesa;
import br.com.tonegobbi.transcrevedespesas.enums.CategoriaDespesa;
import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;
import br.com.tonegobbi.transcrevedespesas.exception.RecursoNaoEncontradoException;
import br.com.tonegobbi.transcrevedespesas.repository.DespesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DespesaService {

    private final DespesaRepository repository;

    public DespesaService(DespesaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DespesaResponse registrarManual(DespesaRequest request) {
        return registrar(request.descricao(), request.valor(), request.categoria(), request.dataDespesa(),
                request.formaPagamento(), request.observacao(), OrigemLancamento.MANUAL, null);
    }

    @Transactional
    public DespesaResponse registrarPorIa(String descricao, BigDecimal valor, CategoriaDespesa categoria,
                                          LocalDate dataDespesa, String formaPagamento, String observacao,
                                          String textoOriginal, OrigemLancamento origem) {
        return registrar(descricao, valor, categoria, dataDespesa, formaPagamento, observacao, origem, textoOriginal);
    }

    private DespesaResponse registrar(String descricao, BigDecimal valor, CategoriaDespesa categoria,
                                      LocalDate dataDespesa, String formaPagamento, String observacao,
                                      OrigemLancamento origem, String textoOriginal) {
        var data = dataDespesa != null ? dataDespesa : LocalDate.now();
        var despesa = new Despesa(descricao, valor, categoria, data, formaPagamento, observacao, origem, textoOriginal);
        return DespesaResponse.from(repository.save(despesa));
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> listar() {
        return repository.findAllByOrderByDataDespesaDescIdDesc().stream().map(DespesaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DespesaResponse buscarPorId(Long id) {
        return repository.findById(id).map(DespesaResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Despesa nao encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        return repository.findByDataDespesaBetweenOrderByDataDespesaDescIdDesc(inicio, fim).stream()
                .map(DespesaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> listarPorCategoria(CategoriaDespesa categoria) {
        return repository.findByCategoriaOrderByDataDespesaDescIdDesc(categoria).stream()
                .map(DespesaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TotalResponse totalPorPeriodo(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        return new TotalResponse(repository.totalPorPeriodo(inicio, fim));
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse> totalPorCategoria() {
        return repository.totalPorCategoria().stream()
                .map(item -> new TotalCategoriaResponse(item.getCategoria(), item.getTotal())).toList();
    }

    @Transactional(readOnly = true)
    public DespesaResponse maiorDespesa() {
        return repository.findTopByOrderByValorDescIdDesc().map(DespesaResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma despesa registrada"));
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Periodo invalido");
        }
    }
}
