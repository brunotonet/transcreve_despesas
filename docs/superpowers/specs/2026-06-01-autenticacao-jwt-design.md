# Autenticacao JWT para Ambiente de Desenvolvimento

## Objetivo

Adicionar autenticacao stateless com Spring Security e JWT. A implementacao atende desenvolvimento e testes com dois usuarios fixos em memoria e preserva uma fronteira clara para substituir `InMemoryUserDetailsManager` por persistencia no futuro.

## Usuarios de Teste

| Usuario | Senha | Role |
| --- | --- | --- |
| `influencer` | `password` | `INFLUENCER` |
| `brand` | `password` | `BRAND` |

As senhas sao codificadas com `BCryptPasswordEncoder` na inicializacao.

## Rotas

O login e publico:

```http
POST /auth/login
Content-Type: application/json

{
  "username": "influencer",
  "password": "password"
}
```

Resposta:

```json
{
  "token": "JWT_GERADO_AQUI",
  "type": "Bearer",
  "expiresInMinutes": 60
}
```

As APIs de negocio exigem:

```http
Authorization: Bearer TOKEN
```

Swagger UI e OpenAPI sao excecoes publicas explicitas para permitir a validacao local pelo navegador. Qualquer outra rota nao liberada explicitamente exige autenticacao.

Endpoint de verificacao:

```http
GET /api/me
Authorization: Bearer TOKEN
```

Resposta para `influencer`:

```json
{
  "username": "influencer",
  "authorities": ["ROLE_INFLUENCER"]
}
```

## Arquitetura

```text
POST /auth/login
  -> AuthController
  -> AuthenticationManager
  -> InMemoryUserDetailsManager
  -> BCryptPasswordEncoder
  -> JwtService
  -> JWT assinado

Authorization: Bearer TOKEN
  -> JwtAuthFilter
  -> JwtService
  -> UserDetailsService
  -> SecurityContextHolder
  -> Controller protegido
```

Componentes:

| Classe | Responsabilidade |
| --- | --- |
| `SecurityConfig` | Configura stateless, desabilita CSRF, form login e HTTP Basic, libera login e documentacao e registra filtro JWT. |
| `JwtService` | Gera token, valida assinatura e expiracao e extrai username. |
| `JwtAuthFilter` | Lê Bearer token, autentica usuario valido e segue a cadeia de filtros. |
| `AuthController` | Autentica username e password e devolve JWT. |
| `MeController` | Expõe usuario autenticado e authorities para teste. |

## Dependencias

- `spring-boot-starter-security`
- `io.jsonwebtoken:jjwt-api:0.13.0`
- `io.jsonwebtoken:jjwt-impl:0.13.0`
- `io.jsonwebtoken:jjwt-jackson:0.13.0`

## Configuracao

```yaml
app:
  jwt:
    secret-base64: ${JWT_SECRET_BASE64:SEGREDO_BASE64_APENAS_PARA_DESENVOLVIMENTO}
    expiration-minutes: ${JWT_EXPIRATION_MINUTES:60}
```

O segredo padrao local deve ser trocado via `.env` fora do desenvolvimento. O valor precisa representar ao menos 256 bits para assinatura HMAC SHA-256.

## Respostas de Erro

- Credenciais incorretas em `/auth/login`: HTTP `401 Unauthorized`.
- Ausencia de Bearer token em rota protegida: HTTP `401 Unauthorized`.
- Token invalido ou expirado: HTTP `401 Unauthorized`.
- Usuario autenticado sem permissao futura: HTTP `403 Forbidden`.

## Swagger UI

Swagger UI e OpenAPI sao publicos para simplificar testes locais pelo navegador. O usuario abre `/swagger-ui.html`, executa `POST /auth/login`, copia o JWT e informa somente o token no botao `Authorize`. A interface inclui o header Bearer ao executar APIs protegidas. A especificacao OpenAPI declara o esquema Bearer JWT.

## Testes

- `JwtServiceTest`: gera, extrai username, valida token e rejeita token expirado.
- `JwtAuthFilterTest`: autentica Bearer valido e ignora requisicao sem token.
- `AuthControllerTest`: login valido retorna JWT; credenciais invalidas retornam `401`.
- Teste HTTP integrado: `/api/me` e rota financeira retornam `401` sem token; `/api/me` retorna usuario e role com token; Swagger e OpenAPI retornam `200` sem token.
