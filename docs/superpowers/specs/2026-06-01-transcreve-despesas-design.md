# Transcreve Despesas Design

## Escopo

API REST para um unico orcamento local. Registra e consulta despesas manualmente ou por linguagem natural. Entradas de audio sao transcritas com Whisper, interpretadas com Tool Calling e devolvidas como JSON. A sintese MP3 fica em endpoint separado.

## Arquitetura

A aplicacao usa camadas pragmaticas: controller, service e repository. A integracao com IA fica em `service.ai`, audio em `service.audio` e as funcoes disponiveis para o modelo em `tool`. A entidade `InteracaoIa` registra conversa, origem, texto original, transcricao e resposta. Uma janela recente por `conversationId` e reconstruida do PostgreSQL antes de cada nova chamada.

## Persistencia

`Despesa` armazena descricao, valor, categoria, data, forma de pagamento, observacao, origem, texto original e timestamps. `InteracaoIa` armazena a trilha conversacional. PostgreSQL sobe por Docker Compose. Flyway versiona o schema; Hibernate apenas valida.

## API

- `POST /api/despesas`
- `GET /api/despesas`
- `GET /api/despesas/{id}`
- `GET /api/despesas/periodo?inicio=&fim=`
- `GET /api/despesas/categoria/{categoria}`
- `GET /api/despesas/totais/periodo?inicio=&fim=`
- `GET /api/despesas/totais/categoria`
- `POST /api/assistente/texto`
- `POST /api/assistente/audio`
- `POST /api/assistente/sintese`
- `GET /api/interacoes/{conversationId}`

## Erros

Entradas invalidas retornam HTTP 400 com `ProblemDetail`. Recursos inexistentes retornam HTTP 404. Falhas de IA e audio retornam HTTP 502 sem expor segredos.

## Testes

Testes unitarios cobrem regras de despesas, ferramentas e historico. Chamadas externas reais ficam fora da suite automatizada padrao e sao verificadas pelo roteiro de validacao integrada descrito no README.
