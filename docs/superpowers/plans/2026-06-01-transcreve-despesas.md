# Transcreve Despesas Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar uma API Spring Boot para registrar, consultar e interpretar despesas por texto e audio.

**Architecture:** Camadas pragmaticas com services testaveis, Spring Data JPA, ferramentas Spring AI e historico conversacional persistente. Integracoes externas ficam isoladas em services de IA e audio.

**Tech Stack:** Java 21, Maven, Spring Boot 4.0.6, Spring AI 2.0.0-M4, PostgreSQL, Flyway, Docker Compose, Lombok.

---

### Task 1: Dominio e consultas

- [x] Criar testes unitarios do servico de despesas.
- [x] Executar os testes e confirmar falha por classes ausentes.
- [x] Implementar entidade, enums, DTOs, repository e service.
- [x] Executar os testes e confirmar sucesso.

### Task 2: Tool Calling

- [x] Criar testes unitarios das ferramentas financeiras.
- [x] Executar os testes e confirmar falha por classe ausente.
- [x] Implementar ferramentas para registrar, consultar, totalizar e localizar maior despesa.
- [x] Executar os testes e confirmar sucesso.

### Task 3: Historico, IA e audio

- [x] Criar testes unitarios para historico persistente.
- [x] Implementar entidades, repositories e services isolando Spring AI.
- [x] Configurar Whisper `whisper-1`, TTS `gpt-4o-mini-tts` e chat.

### Task 4: REST, banco e documentacao

- [x] Criar controllers e tratamento global de erros.
- [x] Criar migracao Flyway, Docker Compose e README.
- [x] Gerar Maven Wrapper.
- [x] Executar `mvn test` e `mvn package`.
