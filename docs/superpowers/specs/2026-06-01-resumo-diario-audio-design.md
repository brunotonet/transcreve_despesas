# Resumo Diario de Despesas em Audio

## Objetivo

Adicionar um endpoint que retorna MP3 com um resumo das despesas de uma data. Quando a data nao for informada, a aplicacao usa o dia atual. A sintese usa `TextToSpeechModel` do Spring AI com OpenAI e voz `nova`.

## Contrato HTTP

```http
GET /api/assistente/resumo-dia/audio?data=2026-06-01
Accept: audio/mpeg
```

O parametro `data` e opcional. A resposta sempre possui `Content-Type: audio/mpeg` e arquivo `resumo-despesas-dia.mp3`.

## Texto Sintetizado

Quando houver despesas, o texto informa o total e narra cada lancamento individual com valor, descricao e categoria:

```text
O total de gastos deste dia foi 1000 reais.
Voce gastou 500 reais com Oficina, categoria outros.
Voce gastou 300 reais com Mercado, categoria mercado.
Voce gastou 200 reais com Gasolina, categoria combustivel.
```

Quando nao houver despesas:

```text
Nao foram registradas despesas neste dia.
```

## Arquitetura

```text
AssistenteController
  -> ResumoDiarioAudioService
      -> DespesaService.listarPorPeriodo(data, data)
      -> monta texto deterministico
      -> SinteseAudioService
          -> TextToSpeechModel
          -> OpenAI TTS gpt-4o-mini-tts com voz nova
```

O texto e composto localmente para manter soma, itens e testes deterministicos. O Chat Model nao participa deste fluxo.

## Testes

- `SinteseAudioServiceTest`: texto recebido gera retorno de bytes do `TextToSpeechModel`.
- `ResumoDiarioAudioServiceTest`: despesas existentes geram texto com total e lancamentos individuais.
- `ResumoDiarioAudioServiceTest`: dia vazio gera mensagem de ausencia.
- Suite completa Maven e validacao real do endpoint com OpenAI.
