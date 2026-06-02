package br.com.tonegobbi.transcrevedespesas.service.audio;

import br.com.tonegobbi.transcrevedespesas.exception.IntegracaoIaIndisponivelException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SinteseAudioServiceTest {

    @Mock
    private TextToSpeechModel textToSpeechModel;

    @InjectMocks
    private SinteseAudioService service;

    @Test
    void sintetizaTextoComoAudio() {
        var audio = new byte[]{1, 2, 3};
        when(textToSpeechModel.call("Resumo do dia")).thenReturn(audio);

        assertThat(service.sintetizar("Resumo do dia")).isEqualTo(audio);
    }

    @Test
    void informaIndisponibilidadeQuandoOpenAiNaoResponde() {
        when(textToSpeechModel.call("Resumo do dia"))
                .thenThrow(new ResourceAccessException("Connection reset"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.sintetizar("Resumo do dia"))
                .isInstanceOf(IntegracaoIaIndisponivelException.class)
                .hasMessage("Servico de sintese de audio temporariamente indisponivel. Tente novamente.");
    }
}
