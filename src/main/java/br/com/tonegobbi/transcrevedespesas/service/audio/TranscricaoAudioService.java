package br.com.tonegobbi.transcrevedespesas.service.audio;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class TranscricaoAudioService {

    private final TranscriptionModel transcriptionModel;

    public TranscricaoAudioService(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcrever(Resource audio) {
        return transcriptionModel.transcribe(audio);
    }
}
