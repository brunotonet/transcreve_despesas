# Transcreve Despesas

API REST em Java para registrar, consultar e interpretar despesas usando texto e voz. A aplicacao usa OpenAI via Spring AI para interpretar linguagem natural, transcrever audio com Whisper, executar funcoes Java por Tool Calling e gerar respostas em audio.

## Funcionalidades

- Registro manual de despesas por JSON.
- Consultas por ID, periodo e categoria.
- Totais por periodo e categoria.
- Interpretacao de comandos em portugues por IA.
- Registro e consulta de despesas por Tool Calling.
- Transcricao de audio com Whisper `whisper-1`.
- Sintese de respostas em MP3 com `gpt-4o-mini-tts` e voz feminina `nova`.
- Resumo falado do dia com total e despesas individuais.
- Historico persistente por `conversationId`.
- PostgreSQL com migracoes Flyway.
- Swagger UI para documentar e testar os endpoints.
- Spring Security com autenticacao JWT stateless.
- Dois usuarios BCrypt em memoria para desenvolvimento e testes.

## Caso de uso principal: registrar despesa falada pelo usuario

O usuario pode gravar um audio dizendo:

```text
Gastei 400 no mercado.
```

O cliente envia o arquivo gravado para:

```http
POST /api/assistente/audio
Content-Type: multipart/form-data
```

Fluxo executado pela API:

```text
audio do usuario
  -> Whisper transcreve: "Gastei 400 no mercado."
  -> Chat Model interpreta a intencao
  -> Tool Calling executa registrarDespesa(...)
  -> DespesaService grava a despesa
  -> PostgreSQL persiste valor 400, categoria MERCADO e origem AUDIO
  -> API devolve transcricao e confirmacao em JSON
```

Exemplo de envio no PowerShell:

```powershell
$form = @{
    conversationId = "usuario-local"
    file = Get-Item ".\gastei-400-mercado.m4a"
}

Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/assistente/audio" `
    -Headers $headers `
    -Form $form
```

Exemplo de resposta:

```json
{
  "conversationId": "usuario-local",
  "textoOriginal": "Gastei 400 no mercado.",
  "transcricao": "Gastei 400 no mercado.",
  "resposta": "A despesa de 400 reais com mercado foi registrada com sucesso."
}
```

Para conferir o fato persistido:

```powershell
Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/despesas/categoria/MERCADO" `
    -Headers $headers
```

## Versoes e referencias

| Componente | Versao usada | Referencia oficial |
| --- | --- | --- |
| Java | `21` | [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21) |
| Spring Boot | `4.0.6` | [Spring Boot](https://spring.io/projects/spring-boot) |
| Spring AI | `2.0.0-M4` | [Spring AI](https://spring.io/projects/spring-ai) e [artefato no Maven Central](https://repo1.maven.org/maven2/org/springframework/ai/spring-ai-bom/2.0.0-M4/) |
| Spring Security | gerenciado pelo Spring Boot | [Spring Security](https://docs.spring.io/spring-security/reference/) |
| JJWT | `0.13.0` | [JJWT no Maven Central](https://repo1.maven.org/maven2/io/jsonwebtoken/jjwt-api/0.13.0/) |
| springdoc-openapi | `3.0.3` | [springdoc-openapi para Spring Boot 4](https://springdoc.org/v4/) |
| Maven Wrapper | `3.3.4` | [Apache Maven Wrapper](https://maven.apache.org/wrapper/) |
| Maven baixado pelo wrapper | `3.9.9` | [Apache Maven 3.9.9](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/) |
| PostgreSQL | imagem Docker `postgres:16` | [Imagem oficial PostgreSQL](https://hub.docker.com/_/postgres) |
| Flyway | gerenciado pelo Spring Boot | [Flyway](https://documentation.red-gate.com/flyway) |
| OpenAI Chat | `gpt-4o-mini` | [Modelos OpenAI](https://platform.openai.com/docs/models) |
| OpenAI Whisper | `whisper-1` | [Speech to text](https://platform.openai.com/docs/guides/speech-to-text) |
| OpenAI TTS | `gpt-4o-mini-tts`, voz `nova` | [Text to speech](https://platform.openai.com/docs/guides/text-to-speech) |
| Docker Desktop | recomendado: versao atual estavel | [Instalar Docker Desktop](https://docs.docker.com/desktop/setup/install/windows-install/) |

Durante a validacao integrada em 1 de junho de 2026, a tag `postgres:16` resolveu PostgreSQL `16.14`. Como essa tag recebe atualizacoes de patch, uma instalacao posterior pode resolver outra versao `16.x`.

## Arquitetura

```text
Authorization: Bearer JWT -> JwtAuthFilter -> controller -> service -> repository -> PostgreSQL
                 |
                 +-> service.ai -> ChatClient -> DespesaTools
                 |
                 +-> service.audio -> Whisper / TTS
```

O historico da IA fica na tabela `interacoes_ia`. Antes de processar cada mensagem, a aplicacao recupera as ultimas dez interacoes da conversa para reconstruir o contexto apos reinicios.

## Instalar no Windows

### 1. Instalar Java 21

1. Acesse [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21).
2. Baixe o instalador Windows x64 JDK.
3. Execute o instalador e habilite a configuracao de `JAVA_HOME`.
4. Abra um novo PowerShell e valide:

```powershell
java -version
```

O projeto usa Java 21. A validacao integrada tambem foi executada com JDK `26.0.1`.

### 2. Instalar Docker Desktop

1. Acesse [Docker Desktop para Windows](https://docs.docker.com/desktop/setup/install/windows-install/).
2. Instale o Docker Desktop.
3. Inicie o Docker Desktop.
4. Aguarde o engine ficar disponivel e valide:

```powershell
docker version
docker compose version
```

Nao e necessario instalar PostgreSQL manualmente. O Docker Compose baixa e executa a imagem oficial.

### 3. Baixar o projeto

Se o projeto estiver em um repositorio Git, instale o [Git](https://git-scm.com/downloads) e execute:

```powershell
git clone URL_DO_REPOSITORIO
cd transcreve_despesas
```

Se o projeto for entregue como ZIP:

1. Extraia o arquivo.
2. Abra o PowerShell.
3. Entre na pasta extraida:

```powershell
cd C:\caminho\para\transcreve_despesas
```

Nao e necessario instalar Maven manualmente. O arquivo `mvnw.cmd` baixa a versao configurada.

### 4. Criar uma chave da OpenAI

1. Acesse [OpenAI API Keys](https://platform.openai.com/api-keys).
2. Crie uma chave de API.
3. Verifique se a conta possui creditos para uso da API.
4. Nunca envie a chave ao Git ou a inclua em documentacao.

### 5. Configurar o arquivo `.env`

Copie o exemplo:

```powershell
Copy-Item .env.example .env
```

Edite `.env`:

```dotenv
OPENAI_API_KEY=sk-substitua-pela-chave
DB_URL=jdbc:postgresql://localhost:5432/transcreve_despesas
DB_USER=transcreve
DB_PASSWORD=transcreve
JWT_SECRET_BASE64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=
JWT_EXPIRATION_MINUTES=60
```

O segredo JWT acima serve apenas para desenvolvimento local. Para outro ambiente, gere um valor Base64 forte e exclusivo:

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

O arquivo `.env` esta ignorado pelo Git. Para iniciar a aplicacao pelo PowerShell, carregue as variaveis na sessao atual:

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}
```

## Executar a aplicacao

### 1. Subir o PostgreSQL

```powershell
docker compose --env-file .env up -d postgres
docker compose --env-file .env ps
```

O resultado esperado inclui `postgres:16` com status `healthy`.

Para conferir a versao resolvida do banco:

```powershell
docker compose --env-file .env exec postgres psql -U $env:DB_USER -d transcreve_despesas -c "select version();"
```

### 2. Executar testes automatizados

```powershell
.\mvnw.cmd test
```

Resultado esperado: `18` testes executados, sem falhas ou erros.

### 3. Empacotar a aplicacao

```powershell
.\mvnw.cmd package
```

Resultado esperado:

```text
target\transcreve_despesas-0.0.1-SNAPSHOT.jar
```

### 4. Iniciar a API

```powershell
.\mvnw.cmd spring-boot:run
```

Aguarde a mensagem `Started TranscreveDespesasApplication`. A API fica disponivel em `http://localhost:8080`.

## Teste rapido pelo navegador com Swagger UI

Nao e necessario instalar ou configurar ModHeader. O Swagger UI e a especificacao OpenAPI sao publicos, mas as APIs de negocio continuam protegidas por JWT.

### 1. Abrir o Swagger UI

Abra no navegador:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 2. Gerar o token JWT

Na pagina do Swagger:

1. Localize e expanda `POST /auth/login`.
2. Clique em `Try it out`.
3. No campo `Request body`, informe um dos usuarios fixos de desenvolvimento.

Para entrar como `influencer`, use exatamente:

```json
{
  "username": "influencer",
  "password": "password"
}
```

Tambem existe o usuario `brand`:

```json
{
  "username": "brand",
  "password": "password"
}
```

4. Clique em `Execute`.
5. Confirme que a resposta possui HTTP `200`.
6. Na resposta JSON, copie somente o valor do campo `token`.

Exemplo de resposta:

```json
{
  "token": "JWT_GERADO_AQUI",
  "type": "Bearer",
  "expiresInMinutes": 60
}
```

### 3. Autorizar as chamadas protegidas

1. Clique no botao `Authorize`, localizado na parte superior direita do Swagger UI.
2. Cole somente o JWT copiado do campo `token`.
3. Nao escreva `Bearer ` antes do token. O Swagger adiciona esse prefixo automaticamente.
4. Clique em `Authorize`.
5. Clique em `Close`.

### 4. Confirmar o usuario autenticado

1. Localize e expanda `GET /api/me`.
2. Clique em `Try it out`.
3. Clique em `Execute`.
4. Confirme que a resposta possui HTTP `200`.

Resposta esperada para o usuario `influencer`:

```json
{
  "username": "influencer",
  "authorities": [
    "ROLE_INFLUENCER"
  ]
}
```

### 5. Validar uma API de negocio

1. Localize e expanda `GET /api/despesas`.
2. Clique em `Try it out`.
3. Clique em `Execute`.
4. Confirme que a resposta possui HTTP `200`.

Sem gerar o token ou sem clicar em `Authorize`, as APIs `/api/**` retornam HTTP `401 Unauthorized`.

## Autenticacao JWT

`POST /auth/login`, Swagger UI e a especificacao OpenAPI sao publicos para facilitar a validacao local. Todas as APIs de negocio exigem `Authorization: Bearer TOKEN`.

Usuarios fixos para desenvolvimento:

| Usuario | Senha | Role |
| --- | --- | --- |
| `influencer` | `password` | `ROLE_INFLUENCER` |
| `brand` | `password` | `ROLE_BRAND` |

Obtenha um token e monte o header:

```powershell
$login = @{
    username = "influencer"
    password = "password"
} | ConvertTo-Json

$auth = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/auth/login" `
    -ContentType "application/json" `
    -Body $login

$headers = @{
    Authorization = "$($auth.type) $($auth.token)"
}

Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/me" `
    -Headers $headers
```

O token expira em `60` minutos por padrao. A aplicacao nao cria sessao no servidor.

## Swagger UI e OpenAPI

Swagger UI e OpenAPI sao publicos para permitir testes pelo navegador sem extensoes:

| Recurso | Endereco |
| --- | --- |
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| Especificacao OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| Especificacao OpenAPI YAML | [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) |

O contrato OpenAPI declara o esquema `bearerAuth`. Para testar as APIs protegidas diretamente no navegador:

1. Abra [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
2. Expanda `POST /auth/login`, clique em `Try it out` e envie `{"username":"influencer","password":"password"}`.
3. Copie somente o valor do campo `token` retornado.
4. Clique em `Authorize`, informe o token JWT sem acrescentar `Bearer ` e confirme.
5. Execute os endpoints `/api/**`. O Swagger UI inclui o header `Authorization: Bearer TOKEN` automaticamente.

Nao e necessario instalar ou configurar ModHeader. Sem um token valido, as APIs de negocio continuam retornando HTTP `401`.

Para testar o fluxo falado, use `POST /api/assistente/audio`, informe um `conversationId` opcional e anexe um arquivo real de audio no campo `file`. O endpoint transcreve o arquivo com Whisper, interpreta o texto e registra ou consulta despesas por Tool Calling. Os endpoints de IA consomem a API OpenAI e podem gerar cobranca na conta configurada.

Para ouvir o total e cada lancamento individual de um dia, use `GET /api/assistente/resumo-dia/audio`. Informe `data` no formato `yyyy-MM-dd` ou deixe o campo vazio para usar o dia atual.

## Validar a solucao

Execute os comandos abaixo em outro PowerShell. Primeiro siga a secao `Autenticacao JWT` para preencher `$headers`. Carregue o `.env` nesse terminal caso precise acessar variaveis como `DB_USER`.

### 1. Validar registro manual e PostgreSQL

```powershell
$body = @{
    descricao = "Mercado"
    valor = 89.90
    categoria = "MERCADO"
    dataDespesa = "2026-06-01"
    formaPagamento = "PIX"
    observacao = "Compras da semana"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/despesas" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $body

Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/despesas" `
    -Headers $headers
```

Confirme que a resposta contem `categoria` igual a `MERCADO` e `origemLancamento` igual a `MANUAL`.

### 2. Validar texto, OpenAI e Tool Calling

```powershell
$body = @{
    conversationId = "validacao-texto"
    mensagem = "Registre uma despesa de 45 reais com gasolina hoje na categoria combustivel."
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/assistente/texto" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $body

Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/despesas/categoria/COMBUSTIVEL" `
    -Headers $headers
```

Confirme que existe uma despesa com valor `45`, categoria `COMBUSTIVEL` e origem `TEXTO`.

### 3. Validar consulta em linguagem natural e historico

```powershell
$body = @{
    conversationId = "validacao-texto"
    mensagem = "Quanto gastei com combustivel hoje?"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/assistente/texto" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $body

Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/interacoes/validacao-texto" `
    -Headers $headers
```

Confirme que a IA consulta os dados reais e que o historico contem as duas interacoes da conversa.

### 4. Validar TTS com `gpt-4o-mini-tts`

```powershell
$body = @{
    texto = "Registre uma despesa de trinta e dois reais com mercado hoje na categoria mercado."
} | ConvertTo-Json

Invoke-WebRequest -Method Post `
    -Uri "http://localhost:8080/api/assistente/sintese" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $body `
    -OutFile ".\validacao-tts.mp3"

Get-Item .\validacao-tts.mp3
```

Confirme que `validacao-tts.mp3` foi criado e possui tamanho maior que zero.

O modelo TTS usa a voz feminina `nova`, configurada em `src/main/resources/application.yml`.

### 5. Validar Whisper e fluxo completo de audio

Para uma validacao artificial reproduzivel, use o MP3 gerado na etapa anterior. Para validar o caso real, substitua o caminho por um audio gravado pelo usuario dizendo, por exemplo, `Gastei 400 no mercado`.

```powershell
$form = @{
    conversationId = "validacao-audio"
    file = Get-Item ".\validacao-tts.mp3"
}

Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/assistente/audio" `
    -Headers $headers `
    -Form $form

Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/despesas/categoria/MERCADO" `
    -Headers $headers
```

Confirme que a resposta contem `transcricao`, `resposta` e `conversationId`. Confirme tambem que a despesa interpretada possui origem `AUDIO`.

### 6. Validar agregacoes REST

```powershell
Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/despesas/totais/periodo?inicio=2026-06-01&fim=2026-06-30" `
    -Headers $headers

Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/despesas/totais/categoria" `
    -Headers $headers
```

### 7. Validar Swagger UI e especificacao OpenAPI

Confirme que Swagger UI e OpenAPI podem ser abertos anonimamente:

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html"
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/v3/api-docs"
```

Depois siga o passo a passo da secao `Swagger UI e OpenAPI`: gere o JWT em `POST /auth/login`, use `Authorize` e valide uma chamada protegida como `GET /api/me`.

### 8. Validar resumo diario em audio

O endpoint consulta as despesas persistidas, soma o total e narra cada lancamento individual com sua categoria:

```powershell
Invoke-WebRequest -Method Get `
    -Uri "http://localhost:8080/api/assistente/resumo-dia/audio?data=2026-06-01" `
    -Headers $headers `
    -OutFile ".\resumo-despesas-dia.mp3"

Get-Item .\resumo-despesas-dia.mp3
```

Quando `data` nao for informada, a API usa o dia atual:

```powershell
Invoke-WebRequest -Method Get `
    -Uri "http://localhost:8080/api/assistente/resumo-dia/audio" `
    -Headers $headers `
    -OutFile ".\resumo-despesas-hoje.mp3"
```

Quando nao existem lancamentos, o MP3 informa: `Nao foram registradas despesas neste dia.`

A geracao do MP3 depende da API OpenAI e normalmente leva alguns segundos. Quando a conexao externa continua indisponivel apos tres tentativas curtas, a API retorna HTTP `503 Service Unavailable` com uma mensagem para tentar novamente.

## Endpoints

| Metodo | Endpoint | Finalidade |
| --- | --- | --- |
| POST | `/auth/login` | Autentica usuario e devolve JWT; rota publica |
| GET | `/api/me` | Retorna usuario autenticado e authorities |
| POST | `/api/despesas` | Registra despesa manual |
| GET | `/api/despesas` | Lista despesas |
| GET | `/api/despesas/{id}` | Consulta por ID |
| GET | `/api/despesas/periodo?inicio=&fim=` | Consulta por periodo |
| GET | `/api/despesas/categoria/{categoria}` | Consulta por categoria |
| GET | `/api/despesas/totais/periodo?inicio=&fim=` | Total do periodo |
| GET | `/api/despesas/totais/categoria` | Totais agrupados |
| POST | `/api/assistente/texto` | Interpreta comando textual |
| POST | `/api/assistente/audio` | Transcreve e interpreta audio |
| POST | `/api/assistente/sintese` | Gera MP3 de uma resposta |
| GET | `/api/assistente/resumo-dia/audio?data=` | Gera MP3 com total diario e despesas individuais |
| GET | `/api/interacoes/{conversationId}` | Lista historico persistido |
| GET | `/swagger-ui.html` | Abre a interface Swagger UI; rota publica |
| GET | `/v3/api-docs` | Retorna a especificacao OpenAPI em JSON; rota publica |
| GET | `/v3/api-docs.yaml` | Retorna a especificacao OpenAPI em YAML; rota publica |

## Encerrar o ambiente

Interrompa a API com `Ctrl+C`. Para parar o PostgreSQL:

```powershell
docker compose --env-file .env down
```

Para remover tambem os dados locais:

```powershell
docker compose --env-file .env down -v
```

Use `down -v` somente quando quiser apagar permanentemente as despesas persistidas.

## Solucionar problemas comuns

### Docker nao responde

Confirme que o Docker Desktop esta aberto:

```powershell
docker info
```

### Porta `5432` ocupada

Outro PostgreSQL pode estar usando a porta. Pare o servico local ou altere o mapeamento de porta em `docker-compose.yml` e atualize `DB_URL`.

### API nao inicia por erro de banco

Confirme que as mesmas credenciais existem no `.env` e foram carregadas no PowerShell. Depois confira:

```powershell
docker compose --env-file .env ps
```

### OpenAI retorna erro de autenticacao ou saldo

Confirme a chave em [OpenAI API Keys](https://platform.openai.com/api-keys) e verifique creditos na [pagina de billing](https://platform.openai.com/settings/organization/billing/overview).

### Resumo em audio demora ou retorna HTTP `503`

A sintese depende de `https://api.openai.com/v1/audio/speech`. A aplicacao limita a espera com tres tentativas curtas. Quando receber HTTP `503`, aguarde alguns segundos e execute novamente.

### API retorna HTTP `401`

Confirme que executou `POST /auth/login` e incluiu `Authorization: Bearer TOKEN` na chamada. Tokens expiram apos `JWT_EXPIRATION_MINUTES`, com padrao de `60` minutos.

## Documentação técnica

Consulte [docs/RELATORIO_TECNICO.md](docs/RELATORIO_TECNICO.md) para arquitetura, decisoes e evidencias da validacao integrada.
