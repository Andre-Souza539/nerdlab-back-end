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
@Tag(name = "Diagnóstico", description = "Endpoint de teste de conectividade e verificação de disponibilidade")
public class PingController {

    private final UserService userService;

    public PingController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
            summary = "Teste de conectividade pública (Ping/Pong)",
            description = "Responde 'pong' com o timestamp do servidor para validação de liveness. Se enviado Bearer Token válido, enriquece a resposta com os dados do usuário autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Servidor ativo e responsivo",
            content = @Content(schema = @Schema(implementation = PingResponse.class)))
    public ResponseEntity<PingResponse> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSummaryResponse userSummary = null;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                userSummary = userService.getProfile(authentication.getName());
            } catch (Exception ignored) {
                // Mantém null em caso de erro na resolução de perfil durante o ping
            }
        }

        PingResponse response = new PingResponse(
                "pong",
                Instant.now(),
                userSummary
        );
        return ResponseEntity.ok(response);
    }
}
