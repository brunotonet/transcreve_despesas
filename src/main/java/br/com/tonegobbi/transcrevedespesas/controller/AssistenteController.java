package br.com.tonegobbi.transcrevedespesas.controller;

import br.com.tonegobbi.transcrevedespesas.dto.AssistenteResponse;
import br.com.tonegobbi.transcrevedespesas.dto.AssistenteTextoRequest;
import br.com.tonegobbi.transcrevedespesas.dto.SinteseRequest;
import br.com.tonegobbi.transcrevedespesas.service.ai.AssistenteIaService;
import br.com.tonegobbi.transcrevedespesas.service.audio.ResumoDiarioAudioService;
import br.com.tonegobbi.transcrevedespesas.service.audio.SinteseAudioService;
import br.com.tonegobbi.transcrevedespesas.service.audio.TranscricaoAudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/assistente")
@Tag(name = "Assistente", description = "Interpretacao de despesas por texto ou audio e sintese de voz")
public class AssistenteController {

    private final AssistenteIaService assistenteIaService;
    private final TranscricaoAudioService transcricaoAudioService;
    private final SinteseAudioService sinteseAudioService;
    private final ResumoDiarioAudioService resumoDiarioAudioService;

    public AssistenteController(AssistenteIaService assistenteIaService, TranscricaoAudioService transcricaoAudioService,
                                SinteseAudioService sinteseAudioService,
                                ResumoDiarioAudioService resumoDiarioAudioService) {
        this.assistenteIaService = assistenteIaService;
        this.transcricaoAudioService = transcricaoAudioService;
        this.sinteseAudioService = sinteseAudioService;
        this.resumoDiarioAudioService = resumoDiarioAudioService;
    }

    @PostMapping("/texto")
    @Operation(summary = "Interpretar comando textual", description = "Interpreta linguagem natural e executa Tool Calling para registrar ou consultar despesas.")
    public AssistenteResponse texto(@Valid @RequestBody AssistenteTextoRequest request) {
        return assistenteIaService.processarTexto(request.conversationId(), request.mensagem());
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Transcrever e interpretar audio", description = "Recebe o audio do usuario, transcreve com Whisper e executa Tool Calling para registrar ou consultar despesas.")
    public AssistenteResponse audio(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "conversationId", required = false) String conversationId) {
        var transcricao = transcricaoAudioService.transcrever(file.getResource());
        return assistenteIaService.processarAudio(conversationId, transcricao);
    }

    @PostMapping(value = "/sintese", produces = "audio/mpeg")
    @Operation(summary = "Gerar resposta em audio", description = "Converte texto em MP3 usando o modelo OpenAI gpt-4o-mini-tts.")
    public ResponseEntity<byte[]> sintese(@Valid @RequestBody SinteseRequest request) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resposta.mp3\"")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(sinteseAudioService.sintetizar(request.texto()));
    }

    @GetMapping(value = "/resumo-dia/audio", produces = "audio/mpeg")
    @Operation(summary = "Gerar resumo diario em audio", description = "Retorna MP3 com o total e cada despesa individual da data. Quando a data nao e informada, usa o dia atual.")
    public ResponseEntity<byte[]> resumoDiaAudio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        var dataResumo = data != null ? data : LocalDate.now();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resumo-despesas-dia.mp3\"")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resumoDiarioAudioService.sintetizarResumo(dataResumo));
    }
}
