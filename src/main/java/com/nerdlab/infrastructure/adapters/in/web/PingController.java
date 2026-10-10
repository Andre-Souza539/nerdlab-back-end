package com.nerdlab.infrastructure.adapters.in.web;

import com.nerdlab.application.services.UserService;
import com.nerdlab.infrastructure.adapters.in.web.dto.PingResponse;
import com.nerdlab.infrastructure.adapters.in.web.dto.UserSummaryResponse;
import com.nerdlab.infrastructure.adapters.in.web.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/ping")
@Tag(name = "Diagnóstico", description = "Endpoint de teste de conectividade e validação de sessão protegida")
public class PingController {

    private final UserService userService;

    public PingController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
            summary = "Teste de conectividade protegido (Ping/Pong)",
            description = "Responde 'pong' com o timestamp do servidor e os dados do usuário autenticado. Requer Bearer Token JWT."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Conexão autenticada confirmada com sucesso",
            content = @Content(schema = @Schema(implementation = PingResponse.class)))
    @ApiResponse(responseCode = "401", description = "Acesso negado: Bearer token ausente ou inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<PingResponse> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication.getName();
        UserSummaryResponse userSummary = userService.getProfile(identifier);

        PingResponse response = new PingResponse(
                "pong",
                Instant.now(),
                userSummary
        );
        return ResponseEntity.ok(response);
    }
}
