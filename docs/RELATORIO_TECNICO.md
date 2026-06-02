# Relatorio Tecnico

## Objetivo

`transcreve_despesas` e uma API REST para um unico orcamento local. A aplicacao registra, consulta e interpreta despesas por texto ou audio. A integracao com OpenAI usa Spring AI e Tool Calling para executar funcoes Java sobre dados reais persistidos em PostgreSQL.

## Versoes

| Componente | Versao configurada | Referencia oficial |
| --- | --- | --- |
| Java | `21` | [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21) |
| Maven Wrapper | `3.3.4` | [Apache Maven Wrapper](https://maven.apache.org/wrapper/) |
| Maven | `3.9.9` | [Apache Maven 3.9.9](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/) |
| Spring Boot | `4.0.6` | [Spring Boot](https://spring.io/projects/spring-boot) |
| Spring AI | `2.0.0-M4` | [Spring AI](https://spring.io/projects/spring-ai) e [Maven Central](https://repo1.maven.org/maven2/org/springframework/ai/spring-ai-bom/2.0.0-M4/) |
| Spring Security | gerenciado pelo Spring Boot | [Spring Security](https://docs.spring.io/spring-security/reference/) |
| JJWT | `0.13.0` | [JJWT no Maven Central](https://repo1.maven.org/maven2/io/jsonwebtoken/jjwt-api/0.13.0/) |
| springdoc-openapi | `3.0.3` | [springdoc-openapi para Spring Boot 4](https://springdoc.org/v4/) |
| PostgreSQL | imagem `postgres:16` | [Imagem oficial PostgreSQL](https://hub.docker.com/_/postgres) |
| Flyway | gerenciado pelo Spring Boot | [Flyway](https://documentation.red-gate.com/flyway) |
| OpenAI Chat | `gpt-4o-mini` | [Modelos OpenAI](https://platform.openai.com/docs/models) |
| OpenAI Whisper | `whisper-1` | [Speech to text](https://platform.openai.com/docs/guides/speech-to-text) |
| OpenAI TTS | `gpt-4o-mini-tts`, voz `nova` | [Text to speech](https://platform.openai.com/docs/guides/text-to-speech) |

Na validacao integrada executada em 1 de junho de 2026, a tag `postgres:16` resolveu PostgreSQL `16.14`. A mesma tag pode receber novas versoes de patch no futuro.

## Arquitetura

```text
HTTP
 |
 +-> JwtAuthFilter -> controller
      |
      +-> service
           |
           +-> repository -> PostgreSQL
           +-> service.ai -> OpenAI Chat -> Tool Calling -> DespesaTools
           +-> service.audio -> Whisper / TTS
```

Pacotes principais:

| Pacote | Responsabilidade |
| --- | --- |
| `controller` | Endpoints REST |
| `config` | Metadados OpenAPI exibidos pelo Swagger UI |
| `dto` | Contratos de entrada e saida |
| `entity` | Entidades JPA `Despesa` e `InteracaoIa` |
| `enums` | Categorias e origens de lancamento |
| `repository` | Persistencia Spring Data JPA |
| `service` | Regras de despesas e historico |
| `service.ai` | Orquestracao de chat e contexto persistente |
| `service.audio` | Transcricao Whisper e sintese TTS |
| `tool` | Funcoes Java disponiveis ao modelo |
| `security` | Geracao, validacao e leitura do JWT |

## Autenticacao JWT

`POST /auth/login`, Swagger UI e OpenAPI sao publicos para facilitar a validacao local pelo navegador. Todas as APIs de negocio exigem `Authorization: Bearer TOKEN`. A API usa `SessionCreationPolicy.STATELESS`, CSRF desabilitado, form login desabilitado e HTTP Basic desabilitado.

Usuarios locais de desenvolvimento:

| Usuario | Senha | Authority |
| --- | --- | --- |
| `influencer` | `password` | `ROLE_INFLUENCER` |
| `brand` | `password` | `ROLE_BRAND` |

As senhas sao codificadas com BCrypt na inicializacao. `InMemoryUserDetailsManager` pode ser substituido futuramente por um `UserDetailsService` persistente sem alterar `JwtService` ou `JwtAuthFilter`.

## Persistencia

O Docker Compose executa PostgreSQL a partir da imagem oficial `postgres:16`. O schema e versionado por Flyway:

```text
src/main/resources/db/migration/V1__criar_tabelas.sql
```

Tabelas:

| Tabela | Finalidade |
| --- | --- |
| `despesas` | Despesas manuais ou interpretadas pela IA |
| `interacoes_ia` | Historico persistente por `conversationId` |
| `flyway_schema_history` | Controle das migracoes executadas |

O Hibernate usa `ddl-auto=validate`: a aplicacao falha ao iniciar quando o schema nao corresponde ao modelo.

## Integracao com IA

### Texto

`POST /api/assistente/texto` recebe uma mensagem e um `conversationId`. `AssistenteIaService` recupera as dez interacoes mais recentes, envia o contexto ao Chat Model e disponibiliza `DespesaTools`.

### Tool Calling

As ferramentas executam funcoes Java reais para:

- registrar despesa;
- listar despesas por periodo;
- listar despesas por categoria;
- consultar total por periodo;
- consultar totais por categoria;
- consultar maior despesa.

### Audio

`POST /api/assistente/audio` recebe um arquivo multipart, transcreve com `whisper-1`, envia o texto ao assistente e retorna JSON com transcricao e resposta. `POST /api/assistente/sintese` gera MP3 usando `gpt-4o-mini-tts` e voz feminina `nova`.

Exemplo do fluxo principal:

```text
Usuario fala: "Gastei 400 no mercado."
  -> AssistenteController.audio(...)
  -> TranscricaoAudioService.transcrever(...)
  -> Whisper retorna o texto
  -> AssistenteIaService.processarAudio(...)
  -> DespesaTools.registrarDespesa(...)
  -> DespesaService.registrarPorIa(...)
  -> DespesaRepository.save(...)
  -> tabela despesas: valor 400, categoria MERCADO, origem AUDIO
```

### Resumo diario em audio

`GET /api/assistente/resumo-dia/audio?data=yyyy-MM-dd` consulta as despesas persistidas, calcula o total localmente e gera MP3 com total, descricao, valor e categoria de cada lancamento. Quando `data` nao e informada, usa a data atual. Quando nao existem despesas, o audio informa que nao foram registradas despesas neste dia.

O Chat Model nao participa desse fluxo. A aplicacao calcula o resumo a partir do banco antes de chamar `TextToSpeechModel`, evitando delegar somas financeiras ao modelo de linguagem.

## Swagger UI e OpenAPI

A documentacao e gerada por `springdoc-openapi 3.0.3`, versao da linha compativel com Spring Boot 4. O contrato declara `bearerAuth`. Swagger UI e OpenAPI sao publicos para que o usuario abra a interface pelo navegador, execute `POST /auth/login`, copie o JWT e informe o token no botao `Authorize`. O Swagger UI passa a incluir o header Bearer nas chamadas protegidas.

| Recurso | Endereco local |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |

O endpoint multipart `POST /api/assistente/audio` permite anexar um arquivo real na interface Swagger UI. O fluxo executa Whisper, interpreta o texto transcrito por Tool Calling e persiste o fato encontrado quando a fala representa uma despesa.

## Decisoes tecnicas

- Valores monetarios usam `BigDecimal`.
- Datas usam `LocalDate` no dominio.
- O resumo diario calcula valores localmente e usa TTS somente para converter o texto final em MP3.
- Chamadas Spring AI usam tres tentativas curtas: intervalos de `1s`, `2s` e limite de `4s`.
- A fronteira de Tool Calling recebe datas ISO como `String` e converte explicitamente para `LocalDate`.
- OpenFeign nao foi incluido: Spring AI ja fornece os clientes necessarios para OpenAI e nao existe outra API REST externa.
- O historico conversacional e persistido para sobreviver a reinicios.
- O arquivo `.env` nao deve ser versionado.
- JWT usa assinatura HMAC, expira em `60` minutos por padrao e nao cria sessao no servidor.
- O segredo JWT padrao serve apenas para desenvolvimento e deve ser substituido por `JWT_SECRET_BASE64`.

## Ajustes encontrados durante a validacao

### Flyway no Spring Boot 4

Spring Boot 4 modularizou a autoconfiguracao do Flyway. Alem de `flyway-database-postgresql`, a aplicacao precisa declarar `spring-boot-starter-flyway`. Referencia: [Spring Boot build systems](https://docs.spring.io/spring-boot/4.0/reference/using/build-systems.html).

### Datas nas ferramentas do Spring AI

Durante uma chamada real, Spring AI `2.0.0-M4` nao converteu automaticamente um argumento ISO `yyyy-MM-dd` para `LocalDate`. A fronteira das tools foi ajustada para receber `String` e converter a data dentro da aplicacao.

### Credenciais do PostgreSQL

O Docker Compose usa `DB_USER` e `DB_PASSWORD` do `.env`, preservando valores padrao para desenvolvimento local. Isso mantem banco e aplicacao alinhados.

### Indisponibilidade temporaria da OpenAI TTS

Durante a validacao do resumo diario, a conexao com `https://api.openai.com/v1/audio/speech` apresentou `java.net.SocketException: Connection reset`. O banco continuou respondendo normalmente. O Spring AI aplicava retries e mantinha o Swagger UI aguardando sem progresso visivel.

A aplicacao agora limita o retry global Spring AI a tres tentativas curtas. Quando a sintese continua indisponivel, `SinteseAudioService` converte a falha de acesso externo em `IntegracaoIaIndisponivelException` e `GlobalExceptionHandler` retorna HTTP `503 Service Unavailable`.

## Evidencias da validacao integrada

Validacao executada em 1 de junho de 2026:

| Item | Resultado |
| --- | --- |
| Docker Engine | `29.4.0` |
| Docker Compose | `5.1.2` |
| PostgreSQL resolvido pela imagem | `16.14` |
| Flyway | migracao `V1` aplicada |
| API | iniciou em `http://localhost:8080` |
| JWT anonimo | `/api/me` e `/api/despesas` retornaram HTTP `401` |
| Swagger UI e OpenAPI publicos | `/swagger-ui.html` redirecionou para a interface, que carregou com HTTP `200`; `/v3/api-docs` e `/v3/api-docs.yaml` retornaram HTTP `200` sem token |
| JWT invalido | `/api/me` retornou HTTP `401` |
| Login `influencer` | JWT Bearer gerado com expiracao de `60` minutos; `/api/me` retornou `ROLE_INFLUENCER` |
| Login `brand` | JWT Bearer gerado; `/api/me` retornou `ROLE_BRAND` |
| OpenAPI publico | HTTP `200`, contrato publicou `13` rotas e esquema `bearerAuth` |
| Registro manual | `89,90`, categoria `MERCADO`, origem `MANUAL` |
| Tool Calling por texto | `45,00`, categoria `COMBUSTIVEL`, origem `TEXTO` |
| TTS com texto e voz `nova` | MP3 gerado com `77568` bytes |
| Resumo diario com despesas | MP3 gerado com `668928` bytes a partir dos lancamentos persistidos de `2026-06-01` |
| Resumo diario sem despesas | HTTP `200`, `audio/mpeg`, MP3 gerado com mensagem de ausencia |
| Resumo diario com JWT | HTTP `200`, `audio/mpeg`, MP3 gerado com `825600` bytes |
| Whisper e Tool Calling por audio | `32,00`, categoria `MERCADO`, origem `AUDIO` |
| Consulta natural | total de combustivel retornado como `45 reais` |
| Historico persistente | conversa textual registrada com duas interacoes |
| Testes automatizados | `18` testes, `0` falhas, `0` erros |

## Guia das classes Java por fluxo de execucao

### 1. Inicializacao da aplicacao

| Ordem | Classe | Responsabilidade | Recebe | Retorna ou produz |
| --- | --- | --- | --- | --- |
| 1 | `TranscreveDespesasApplication` | Ponto de entrada. Executa `SpringApplication.run(...)`, inicia Spring Boot e habilita o escaneamento dos componentes. | Argumentos opcionais da linha de comando. | Contexto Spring, API HTTP, repositories e services ativos. |
| 2 | `OpenApiConfig` | Define titulo, versao e descricao da especificacao OpenAPI usada pelo Swagger UI. | Inicializacao do contexto Spring. | Bean `OpenAPI` disponivel em `/v3/api-docs`. |
| 3 | `SecurityConfig` | Configura politica stateless, usuarios BCrypt em memoria, login e documentacao publicos e APIs de negocio protegidas. | Inicializacao do contexto Spring. | `SecurityFilterChain`, `UserDetailsService`, `PasswordEncoder` e `AuthenticationManager`. |

Durante a inicializacao, Flyway executa `V1__criar_tabelas.sql`. Em seguida, Hibernate valida se as tabelas correspondem às entidades.

### 2. Fluxo de autenticacao JWT

```text
POST /auth/login
  -> AuthController
  -> AuthenticationManager
  -> InMemoryUserDetailsManager
  -> BCryptPasswordEncoder
  -> JwtService
  -> LoginResponse
```

| Ordem | Classe | Responsabilidade | Recebe | Retorna ou produz |
| --- | --- | --- | --- | --- |
| 1 | `AuthController` | Recebe credenciais e solicita autenticacao. | `LoginRequest`. | `LoginResponse` com JWT Bearer e expiracao. |
| 2 | `LoginRequest` | Valida username e password obrigatorios. | JSON do cliente. | Credenciais tipadas. |
| 3 | `SecurityConfig` | Disponibiliza usuarios `influencer` e `brand` com senhas BCrypt. | Configuracao Spring. | `UserDetailsService` em memoria. |
| 4 | `JwtService` | Assina JWT HMAC e define expiracao. | `UserDetails`. | Token JWT. |
| 5 | `LoginResponse` | Contrato de saida do login. | Token, tipo e minutos. | JSON para o cliente. |

Em cada requisicao protegida, `JwtAuthFilter` le `Authorization: Bearer TOKEN`, valida assinatura e expiracao, carrega o usuario e preenche `SecurityContextHolder`. `MeController` expoe `GET /api/me` para confirmar principal e authorities.

### 3. Fluxo de registro manual

```text
POST /api/despesas
  -> DespesaController
  -> DespesaRequest
  -> DespesaService
  -> Despesa
  -> DespesaRepository
  -> PostgreSQL
  -> DespesaResponse
```

| Ordem | Classe | Responsabilidade | Recebe | Retorna ou produz |
| --- | --- | --- | --- | --- |
| 1 | `DespesaController` | Expoe endpoints REST de despesas. O metodo `registrar(...)` atende `POST /api/despesas`. | JSON convertido para `DespesaRequest`. | HTTP `201` com `DespesaResponse`. |
| 2 | `DespesaRequest` | DTO validado com Jakarta Bean Validation. Exige descricao, valor positivo e categoria. | Campos enviados pelo cliente. | Dados tipados para o service. |
| 3 | `DespesaService` | Aplica regras de negocio. `registrarManual(...)` define origem `MANUAL` e usa a data atual quando a data nao foi informada. | `DespesaRequest`. | `DespesaResponse`. |
| 4 | `Despesa` | Entidade JPA principal. Armazena descricao, valor, categoria, data, pagamento, observacao, origem, texto original e timestamps. | Dados normalizados pelo service. | Registro persistivel na tabela `despesas`. |
| 5 | `DespesaRepository` | Interface Spring Data JPA. Executa `save(...)` e consultas financeiras. | Entidade `Despesa`. | Entidade persistida com ID. |
| 6 | `DespesaResponse` | DTO de saida criado por `DespesaResponse.from(...)`. | Entidade persistida. | JSON devolvido ao cliente. |

### 4. Fluxo de texto com IA e Tool Calling

```text
POST /api/assistente/texto
  -> AssistenteController
  -> AssistenteTextoRequest
  -> AssistenteIaService
  -> InteracaoIaService
  -> ChatClient / OpenAI
  -> DespesaTools
  -> DespesaService
  -> PostgreSQL
  -> InteracaoIaService
  -> AssistenteResponse
```

| Ordem | Classe | Responsabilidade | Recebe | Retorna ou produz |
| --- | --- | --- | --- | --- |
| 1 | `AssistenteController` | Recebe comandos em linguagem natural no metodo `texto(...)`. | JSON com `conversationId` opcional e `mensagem`. | `AssistenteResponse`. |
| 2 | `AssistenteTextoRequest` | DTO de entrada. Valida que a mensagem nao esta vazia. | Campos JSON. | Mensagem tipada para o service. |
| 3 | `AssistenteIaService` | Orquestra a conversa. Gera `conversationId` quando necessario, recupera historico, inclui a data atual no prompt e chama o `ChatClient`. | ID da conversa e mensagem. | Resposta textual da IA e registro de historico. |
| 4 | `InteracaoIaService` | Em `contextoRecente(...)`, recupera as dez interacoes mais recentes e as reorganiza em ordem cronologica. | `conversationId`. | Linhas de contexto para o prompt. |
| 5 | `AiRequestContext` | Mantem temporariamente origem e texto original no `ThreadLocal` da requisicao. Permite que a tool registre se o lancamento veio de texto ou audio. | `OrigemLancamento` e texto. | Contexto disponivel durante a execucao da tool. |
| 6 | `DespesaTools` | Expoe metodos anotados com `@Tool`. O modelo escolhe uma funcao conforme a intencao do usuario. | Argumentos estruturados produzidos pela OpenAI. | Resultado real de registro ou consulta. |
| 7 | `DespesaService` | Executa a operacao financeira solicitada pela tool. Para registro via IA, preserva texto original e origem. | Dados interpretados pela tool. | DTO financeiro real. |
| 8 | `InteracaoIaService` | Em `registrar(...)`, persiste pergunta, resposta, origem e transcricao opcional. | Dados da conversa processada. | Nova entidade `InteracaoIa`. |
| 9 | `InteracaoIa` | Entidade JPA do historico conversacional. | Conversa processada. | Registro na tabela `interacoes_ia`. |
| 10 | `InteracaoIaRepository` | Persiste e recupera historico por `conversationId`. | Entidade ou ID da conversa. | Historico ordenado. |
| 11 | `AssistenteResponse` | DTO devolvido pela API. | Resultado do processamento. | JSON com `conversationId`, texto original, transcricao opcional e resposta. |

### 5. Ferramentas disponiveis para a IA

| Metodo em `DespesaTools` | Uso | Recebe | Retorna |
| --- | --- | --- | --- |
| `registrarDespesa(...)` | Registra uma despesa interpretada pela IA. | Descricao, valor, categoria, data ISO, pagamento e observacao. | `DespesaResponse`. |
| `listarDespesasPorPeriodo(...)` | Lista despesas entre duas datas inclusivas. | Inicio e fim no formato `yyyy-MM-dd`. | Lista de `DespesaResponse`. |
| `listarDespesasPorCategoria(...)` | Lista despesas de uma categoria. | `CategoriaDespesa`. | Lista de `DespesaResponse`. |
| `consultarTotalPorPeriodo(...)` | Soma despesas no periodo. | Inicio e fim no formato `yyyy-MM-dd`. | `TotalResponse`. |
| `consultarTotaisPorCategoria()` | Soma despesas agrupadas. | Nenhum argumento. | Lista de `TotalCategoriaResponse`. |
| `consultarMaiorDespesa()` | Busca o maior lancamento. | Nenhum argumento. | `DespesaResponse`. |

### 6. Fluxo de audio com Whisper

```text
POST /api/assistente/audio
  -> AssistenteController
  -> TranscricaoAudioService
  -> OpenAI Whisper
  -> AssistenteIaService
  -> Tool Calling
  -> AssistenteResponse
```

| Ordem | Classe | Responsabilidade | Recebe | Retorna ou produz |
| --- | --- | --- | --- | --- |
| 1 | `AssistenteController` | O metodo `audio(...)` recebe multipart e encaminha o arquivo para transcricao. | `MultipartFile` e `conversationId` opcional. | `AssistenteResponse`. |
| 2 | `TranscricaoAudioService` | Encapsula o `TranscriptionModel` do Spring AI. | `Resource` contendo audio. | Texto transcrito pelo modelo `whisper-1`. |
| 3 | `AssistenteIaService` | Processa a transcricao como mensagem e define origem `AUDIO`. | ID da conversa e transcricao. | Resposta JSON e historico persistido. |
| 4 | `AssistenteResponse` | Inclui o campo `transcricao` preenchido. | Resultado final. | JSON com transcricao e resposta textual. |

### 7. Fluxo de sintese de voz

```text
POST /api/assistente/sintese
  -> AssistenteController
  -> SinteseRequest
  -> SinteseAudioService
  -> OpenAI TTS
  -> MP3
```

| Ordem | Classe | Responsabilidade | Recebe | Retorna ou produz |
| --- | --- | --- | --- | --- |
| 1 | `AssistenteController` | O metodo `sintese(...)` atende a requisicao HTTP. | JSON com texto. | HTTP `200`, `audio/mpeg`, arquivo `resposta.mp3`. |
| 2 | `SinteseRequest` | DTO de entrada. Rejeita texto vazio. | Campo `texto`. | Texto validado. |
| 3 | `SinteseAudioService` | Encapsula `TextToSpeechModel`. | Texto validado. | Bytes MP3 produzidos por `gpt-4o-mini-tts`. |

### 8. Fluxo de resumo diario em audio

```text
GET /api/assistente/resumo-dia/audio?data=2026-06-01
  -> AssistenteController
  -> ResumoDiarioAudioService
  -> DespesaService.listarPorPeriodo(data, data)
  -> SinteseAudioService
  -> TextToSpeechModel
  -> OpenAI TTS com voz nova
  -> MP3
```

| Ordem | Classe | Responsabilidade | Recebe | Retorna ou produz |
| --- | --- | --- | --- | --- |
| 1 | `AssistenteController` | O metodo `resumoDiaAudio(...)` aceita data opcional e usa o dia atual quando omitida. | Query param `data` opcional. | HTTP `200`, `audio/mpeg`, arquivo `resumo-despesas-dia.mp3`. |
| 2 | `ResumoDiarioAudioService` | Consulta os lancamentos, soma valores com `BigDecimal`, monta a narracao individual e trata dia vazio. | `LocalDate`. | Bytes MP3 retornados pelo servico de sintese. |
| 3 | `DespesaService` | Lista as despesas persistidas exatamente na data solicitada. | Mesmo dia como inicio e fim. | Lista de `DespesaResponse`. |
| 4 | `SinteseAudioService` | Envia o texto final ao `TextToSpeechModel`. | Narracao pronta. | MP3 produzido com `gpt-4o-mini-tts` e voz `nova`. |

### 9. Fluxos de consulta REST

`DespesaController` tambem encaminha consultas ao `DespesaService`:

| Endpoint | Metodo do service | Retorno |
| --- | --- | --- |
| `GET /api/despesas` | `listar()` | Lista de `DespesaResponse`. |
| `GET /api/despesas/{id}` | `buscarPorId(...)` | `DespesaResponse` ou HTTP `404`. |
| `GET /api/despesas/periodo` | `listarPorPeriodo(...)` | Lista filtrada por data. |
| `GET /api/despesas/categoria/{categoria}` | `listarPorCategoria(...)` | Lista filtrada por categoria. |
| `GET /api/despesas/totais/periodo` | `totalPorPeriodo(...)` | `TotalResponse`. |
| `GET /api/despesas/totais/categoria` | `totalPorCategoria()` | Lista de `TotalCategoriaResponse`. |

`InteracaoIaController` expoe `GET /api/interacoes/{conversationId}` e devolve uma lista de `InteracaoIaResponse`.

### 10. DTOs auxiliares

| Classe | Finalidade |
| --- | --- |
| `TotalResponse` | Encapsula um total monetario. |
| `TotalCategoriaResponse` | Encapsula categoria e total agregado. |
| `InteracaoIaResponse` | Representa uma interacao persistida na API de historico. |
| `MeResponse` | Representa username e authorities do usuario autenticado. |

### 11. Enums

| Classe | Valores |
| --- | --- |
| `CategoriaDespesa` | `ALIMENTACAO`, `TRANSPORTE`, `MORADIA`, `SAUDE`, `EDUCACAO`, `LAZER`, `MERCADO`, `COMBUSTIVEL`, `CONTAS`, `OUTROS`. |
| `OrigemLancamento` | `TEXTO`, `AUDIO`, `MANUAL`, `IA`. |

### 12. Tratamento de erros

| Classe | Responsabilidade |
| --- | --- |
| `RecursoNaoEncontradoException` | Representa uma despesa inexistente ou ausencia de dados esperados. |
| `GlobalExceptionHandler` | Converte recursos inexistentes em HTTP `404`, requisicoes invalidas em HTTP `400`, credenciais invalidas em HTTP `401` e indisponibilidade da IA em HTTP `503`. |
| `IntegracaoIaIndisponivelException` | Representa falha temporaria de comunicacao com a IA externa durante a sintese. |

### 13. Testes automatizados

| Classe | Cobertura |
| --- | --- |
| `DespesaServiceTest` | Registro manual, total por periodo e filtro por categoria. |
| `DespesaToolsTest` | Registro interpretado pela IA, contexto original e conversao da data ISO. |
| `InteracaoIaServiceTest` | Reconstrucao cronologica do contexto persistido. |
| `OpenApiDocumentationTest` | Inicializa a API com H2 e confirma que `/v3/api-docs` responde HTTP `200` com o titulo documentado. |
| `SinteseAudioServiceTest` | Confirma que texto recebido e encaminhado ao `TextToSpeechModel` e retorna bytes de audio. |
| `ResumoDiarioAudioServiceTest` | Confirma narracao com total e itens individuais e mensagem falada para dia vazio. |
| `GlobalExceptionHandlerTest` | Confirma que indisponibilidade temporaria da integracao retorna HTTP `503`. |
| `JwtServiceTest` | Confirma emissao, leitura, validacao e expiracao do JWT. |
| `JwtAuthFilterTest` | Confirma autenticacao via Bearer e passagem sem header. |
| `SecurityIntegrationTest` | Confirma login, usuarios fixos, `/api/me`, token invalido, HTTP `401` nas APIs anonimas e acesso publico ao Swagger UI e OpenAPI. |

## Instalar e validar

O roteiro completo para baixar dependencias, instalar ferramentas, configurar `.env`, executar testes e validar cada fluxo esta no [README.md](../README.md).
