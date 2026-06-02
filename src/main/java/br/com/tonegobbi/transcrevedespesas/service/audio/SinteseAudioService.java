package br.com.tonegobbi.transcrevedespesas.service.audio;

import br.com.tonegobbi.transcrevedespesas.exception.IntegracaoIaIndisponivelException;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
public class SinteseAudioService {

    private final TextToSpeechModel textToSpeechModel;

    public SinteseAudioService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public byte[] sintetizar(String texto) {
        try {
            return textToSpeechModel.call(texto);
        } catch (ResourceAccessException exception) {
            throw new IntegracaoIaIndisponivelException(
                    "Servico de sintese de audio temporariamente indisponivel. Tente novamente.", exception);
        }
    }
}
