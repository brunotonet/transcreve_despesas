package br.com.tonegobbi.transcrevedespesas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMinutes;
    private final Clock clock;

    @Autowired
    public JwtService(@Value("${app.jwt.secret-base64}") String secretBase64,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this(secretBase64, expirationMinutes, Clock.systemUTC());
    }

    JwtService(String secretBase64, long expirationMinutes, Clock clock) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.expirationMinutes = expirationMinutes;
        this.clock = clock;
    }

    public String gerarToken(UserDetails userDetails) {
        var agora = Instant.now(clock);
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public String extrairUsername(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido(String token, UserDetails userDetails) {
        try {
            var claims = extrairClaims(token);
            return claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().toInstant().isAfter(Instant.now(clock));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long expirationMinutes() {
        return expirationMinutes;
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clock(() -> Date.from(Instant.now(clock)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
