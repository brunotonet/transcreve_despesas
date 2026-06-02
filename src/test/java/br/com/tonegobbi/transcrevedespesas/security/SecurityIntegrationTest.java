package br.com.tonegobbi.transcrevedespesas.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:security;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.ai.openai.api-key=test-key",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=60"
})
class SecurityIntegrationTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void permiteSomenteLoginSemTokenEAcessaMeComBearer() throws Exception {
        assertThat(get("/api/me", null).statusCode()).isEqualTo(401);
        assertThat(get("/api/despesas", null).statusCode()).isEqualTo(401);
        assertThat(get("/v3/api-docs", null).statusCode()).isEqualTo(200);
        assertThat(get("/v3/api-docs.yaml", null).statusCode()).isEqualTo(200);
        assertThat(get("/swagger-ui/index.html", null).statusCode()).isEqualTo(200);
        assertThat(get("/swagger-ui.html", null).statusCode()).isIn(200, 302);

        var login = post("/auth/login", """
                {"username":"influencer","password":"password"}
                """);
        assertThat(login.statusCode()).isEqualTo(200);
        var loginJson = objectMapper.readTree(login.body());
        assertThat(loginJson.get("type").asText()).isEqualTo("Bearer");
        assertThat(loginJson.get("expiresInMinutes").asLong()).isEqualTo(60);
        var token = loginJson.get("token").asText();

        var me = get("/api/me", token);
        assertThat(me.statusCode()).isEqualTo(200);
        assertThat(me.body()).contains("\"username\":\"influencer\"");
        assertThat(me.body()).contains("ROLE_INFLUENCER");
        assertThat(get("/api/me", "token-invalido").statusCode()).isEqualTo(401);
    }

    @Test
    void rejeitaCredenciaisInvalidas() throws Exception {
        var login = post("/auth/login", """
                {"username":"influencer","password":"senha-incorreta"}
                """);

        assertThat(login.statusCode()).isEqualTo(401);
    }

    @Test
    void autenticaUsuarioBrand() throws Exception {
        var login = post("/auth/login", """
                {"username":"brand","password":"password"}
                """);
        var token = objectMapper.readTree(login.body()).get("token").asText();

        var me = get("/api/me", token);

        assertThat(me.statusCode()).isEqualTo(200);
        assertThat(me.body()).contains("\"username\":\"brand\"");
        assertThat(me.body()).contains("ROLE_BRAND");
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
