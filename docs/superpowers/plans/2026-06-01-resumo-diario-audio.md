# Resumo Diario de Despesas em Audio Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Retornar MP3 com voz `nova` contendo o total diario e cada despesa individual, ou uma mensagem falada quando nao houver despesas.

**Architecture:** Criar `ResumoDiarioAudioService` para consultar `DespesaService`, montar texto deterministico e delegar MP3 ao `SinteseAudioService`. Publicar o fluxo em `AssistenteController` sem usar Chat Model para calculos financeiros.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Spring AI 2.0.0-M4, OpenAI `gpt-4o-mini-tts`, JUnit 5, Mockito.

---

### Task 1: Testar a fronteira TTS existente

**Files:**
- Create: `src/test/java/br/com/tonegobbi/transcrevedespesas/service/audio/SinteseAudioServiceTest.java`

- [ ] Escrever teste que injeta `TextToSpeechModel`, chama `sintetizar("Resumo")` e confirma os bytes retornados.
- [ ] Executar `.\mvnw.cmd -q -Dtest=SinteseAudioServiceTest test`.

### Task 2: Implementar resumo diario

**Files:**
- Create: `src/test/java/br/com/tonegobbi/transcrevedespesas/service/audio/ResumoDiarioAudioServiceTest.java`
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/service/audio/ResumoDiarioAudioService.java`

- [ ] Escrever teste vermelho para texto com total e despesas individuais.
- [ ] Escrever teste vermelho para mensagem de dia vazio.
- [ ] Executar os testes e confirmar falha por classe ausente.
- [ ] Implementar composicao deterministica e delegacao ao `SinteseAudioService`.
- [ ] Executar os testes e confirmar sucesso.

### Task 3: Publicar endpoint

**Files:**
- Modify: `src/main/java/br/com/tonegobbi/transcrevedespesas/controller/AssistenteController.java`

- [ ] Injetar `ResumoDiarioAudioService`.
- [ ] Adicionar `GET /api/assistente/resumo-dia/audio` com `data` opcional e resposta `audio/mpeg`.
- [ ] Descrever operacao no Swagger UI.

### Task 4: Documentar e verificar

**Files:**
- Modify: `README.md`
- Modify: `docs/RELATORIO_TECNICO.md`

- [ ] Documentar voz `nova`, endpoint, exemplo e fluxo.
- [ ] Executar `.\mvnw.cmd -q test`.
- [ ] Executar `.\mvnw.cmd -q package`.
- [ ] Reiniciar API e validar endpoint real com OpenAI.
