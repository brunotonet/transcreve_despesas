package br.com.tonegobbi.transcrevedespesas.controller;

import br.com.tonegobbi.transcrevedespesas.dto.LoginRequest;
import br.com.tonegobbi.transcrevedespesas.dto.LoginResponse;
import br.com.tonegobbi.transcrevedespesas.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Login publico para emissao de JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario de teste", description = "Recebe username e password e retorna JWT valido por 60 minutos.")
    @SecurityRequirements
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var userDetails = (UserDetails) authentication.getPrincipal();
        return new LoginResponse(jwtService.gerarToken(userDetails), "Bearer", jwtService.expirationMinutes());
    }
}
