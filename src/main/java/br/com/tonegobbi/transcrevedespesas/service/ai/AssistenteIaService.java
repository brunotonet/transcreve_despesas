package br.com.tonegobbi.transcrevedespesas.service.ai;

import br.com.tonegobbi.transcrevedespesas.dto.AssistenteResponse;
import br.com.tonegobbi.transcrevedespesas.enums.OrigemLancamento;
import br.com.tonegobbi.transcrevedespesas.service.InteracaoIaService;
import br.com.tonegobbi.transcrevedespesas.tool.AiRequestContext;
import br.com.tonegobbi.transcrevedespesas.tool.DespesaTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AssistenteIaService {

    private static final String SYSTEM_PROMPT = """
            Voce e um assistente financeiro pessoal para um unico orcamento local.
            Responda sempre em portugues brasileiro. Use as ferramentas para registrar ou consultar dados reais.
            Nunca invente despesas. Categorize usando apenas os enums disponiveis.
            Interprete referencias relativas de data usando a data atual informada na mensagem.
            """;

    private final ChatClient chatClient;
    private final InteracaoIaService interacaoService;

    public AssistenteIaService(ChatClient.Builder builder, DespesaTools tools, InteracaoIaService interacaoService) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).defaultTools(tools).build();
        this.interacaoService = interacaoService;
    }

    public AssistenteResponse processarTexto(String conversationId, String mensagem) {
        return processar(conversationId, mensagem, null, OrigemLancamento.TEXTO);
    }

    public AssistenteResponse processarAudio(String conversationId, String transcricao) {
        return processar(conversationId, transcricao, transcricao, OrigemLancamento.AUDIO);
    }

    private AssistenteResponse processar(String conversationId, String mensagem, String transcricao,
                                         OrigemLancamento origem) {
        var conversa = StringUtils.hasText(conversationId) ? conversationId : UUID.randomUUID().toString();
        var historico = String.join("\n", interacaoService.contextoRecente(conversa));
        var prompt = "Data atual: " + LocalDate.now() + "\nHistorico recente:\n" + historico
                + "\nNova mensagem do usuario: " + mensagem;
        AiRequestContext.definir(origem, mensagem);
        try {
            var resposta = chatClient.prompt().user(prompt).call().content();
            interacaoService.registrar(conversa, origem, mensagem, transcricao, resposta);
            return new AssistenteResponse(conversa, mensagem, transcricao, resposta);
        } finally {
            AiRequestContext.limpar();
        }
    }
}
