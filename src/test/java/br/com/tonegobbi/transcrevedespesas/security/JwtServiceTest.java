package br.com.tonegobbi.transcrevedespesas.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET_BASE64 = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");

    @Test
    void geraTokenValidoEExtraiUsername() {
        var service = new JwtService(SECRET_BASE64, 60, Clock.fixed(NOW, ZoneOffset.UTC));
        var user = User.withUsername("influencer").password("hash").roles("INFLUENCER").build();

        var token = service.gerarToken(user);

        assertThat(service.extrairUsername(token)).isEqualTo("influencer");
        assertThat(service.tokenValido(token, user)).isTrue();
    }

    @Test
    void rejeitaTokenExpirado() {
        var emissor = new JwtService(SECRET_BASE64, 60, Clock.fixed(NOW, ZoneOffset.UTC));
        var validador = new JwtService(SECRET_BASE64, 60,
                Clock.fixed(NOW.plusSeconds(3601), ZoneOffset.UTC));
        var user = User.withUsername("brand").password("hash").roles("BRAND").build();

        var token = emissor.gerarToken(user);

        assertThat(validador.tokenValido(token, user)).isFalse();
    }
}
