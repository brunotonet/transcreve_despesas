package br.com.tonegobbi.transcrevedespesas.controller;

import br.com.tonegobbi.transcrevedespesas.dto.InteracaoIaResponse;
import br.com.tonegobbi.transcrevedespesas.service.InteracaoIaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interacoes")
@Tag(name = "Interacoes IA", description = "Historico persistente das conversas")
public class InteracaoIaController {

    private final InteracaoIaService service;

    public InteracaoIaController(InteracaoIaService service) {
        this.service = service;
    }

    @GetMapping("/{conversationId}")
    @Operation(summary = "Listar historico por conversationId")
    public List<InteracaoIaResponse> listar(@PathVariable String conversationId) {
        return service.listar(conversationId);
    }
}
