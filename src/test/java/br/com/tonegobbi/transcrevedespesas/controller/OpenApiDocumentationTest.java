package br.com.tonegobbi.transcrevedespesas.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:openapi;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.ai.openai.api-key=test-key",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=60"
})
class OpenApiDocumentationTest {

    @LocalServerPort
    private int port;

    @Test
    void exposesOpenApiDocument() throws Exception {
        var client = HttpClient.newHttpClient();
        var loginRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"username":"influencer","password":"password"}
                        """))
                .build();
        var loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        var matcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(loginResponse.body());
        assertThat(matcher.find()).isTrue();

        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs"))
                .header("Authorization", "Bearer " + matcher.group(1))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"title\":\"Transcreve Despesas API\"");
        assertThat(response.body()).contains("\"/api/assistente/resumo-dia/audio\"");
        assertThat(response.body()).contains("\"bearerAuth\"");
    }
}
