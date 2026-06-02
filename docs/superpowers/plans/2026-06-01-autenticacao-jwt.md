# Autenticacao JWT Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Proteger as APIs de negocio com JWT stateless e liberar `POST /auth/login`, Swagger UI e OpenAPI.

**Architecture:** `SecurityConfig` configura Spring Security com usuarios BCrypt em memoria e registra `JwtAuthFilter`. `JwtService` encapsula JJWT. `AuthController` gera token após autenticação e `MeController` comprova o principal autenticado.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Spring Security, JJWT 0.13.0, JUnit 5.

---

### Task 1: Dependencias e JwtService

**Files:**
- Modify: `pom.xml`
- Create: `src/test/java/br/com/tonegobbi/transcrevedespesas/security/JwtServiceTest.java`
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/security/JwtService.java`

- [ ] Adicionar Spring Security e JJWT.
- [ ] Escrever testes para gerar, extrair username, validar e expirar token.
- [ ] Confirmar vermelho por classe ausente.
- [ ] Implementar `JwtService`.
- [ ] Confirmar testes verdes.

### Task 2: Filtro JWT

**Files:**
- Create: `src/test/java/br/com/tonegobbi/transcrevedespesas/security/JwtAuthFilterTest.java`
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/security/JwtAuthFilter.java`

- [ ] Escrever testes para autenticar Bearer válido e ignorar header ausente.
- [ ] Confirmar vermelho por classe ausente.
- [ ] Implementar filtro com `OncePerRequestFilter`.
- [ ] Confirmar testes verdes.

### Task 3: Login, seguranca stateless e /api/me

**Files:**
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/dto/LoginRequest.java`
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/dto/LoginResponse.java`
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/dto/MeResponse.java`
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/controller/AuthController.java`
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/controller/MeController.java`
- Create: `src/main/java/br/com/tonegobbi/transcrevedespesas/config/SecurityConfig.java`
- Create: `src/test/java/br/com/tonegobbi/transcrevedespesas/security/SecurityIntegrationTest.java`

- [ ] Escrever teste HTTP integrado exigindo JWT nas APIs de negocio e permitindo login e documentacao publicos.
- [ ] Confirmar vermelho.
- [ ] Implementar DTOs, controllers e configuração stateless.
- [ ] Confirmar teste verde.

### Task 4: Swagger e documentacao

**Files:**
- Modify: `src/main/java/br/com/tonegobbi/transcrevedespesas/config/OpenApiConfig.java`
- Modify: `.env.example`
- Modify: `README.md`
- Modify: `docs/RELATORIO_TECNICO.md`

- [ ] Declarar Bearer JWT na especificacao OpenAPI.
- [ ] Documentar configuração, login e exemplos autenticados.
- [ ] Registrar que Swagger e OpenAPI sao publicos para validacao local e que as APIs de negocio exigem JWT.

### Task 5: Verificacao final

- [ ] Executar `.\mvnw.cmd -q test`.
- [ ] Executar `.\mvnw.cmd -q package`.
- [ ] Reiniciar API.
- [ ] Confirmar `401` sem token.
- [ ] Fazer login real com `influencer`.
- [ ] Confirmar `/api/me` e rota financeira com Bearer token.
