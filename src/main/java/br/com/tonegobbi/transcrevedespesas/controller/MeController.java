package br.com.tonegobbi.transcrevedespesas.controller;

import br.com.tonegobbi.transcrevedespesas.dto.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Usuario autenticado", description = "Validacao do principal JWT")
public class MeController {

    @GetMapping("/me")
    @Operation(summary = "Consultar usuario autenticado")
    public MeResponse me(Authentication authentication) {
        var authorities = authentication.getAuthorities().stream()
                .map(Object::toString)
                .sorted()
                .toList();
        return new MeResponse(authentication.getName(), authorities);
    }
}
