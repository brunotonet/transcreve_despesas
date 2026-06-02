package br.com.tonegobbi.transcrevedespesas.repository;

import br.com.tonegobbi.transcrevedespesas.entity.InteracaoIa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteracaoIaRepository extends JpaRepository<InteracaoIa, Long> {
    List<InteracaoIa> findTop10ByConversationIdOrderByCreatedAtDesc(String conversationId);
    List<InteracaoIa> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}
