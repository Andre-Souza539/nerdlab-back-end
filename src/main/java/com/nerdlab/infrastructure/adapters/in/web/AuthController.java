package com.nerdlab.infrastructure.adapters.in.web;

import com.nerdlab.application.services.UserService;
import com.nerdlab.infrastructure.adapters.in.web.dto.AuthResponse;
import com.nerdlab.infrastructure.adapters.in.web.dto.ChangePasswordRequest;
import com.nerdlab.infrastructure.adapters.in.web.dto.LoginRequest;
import com.nerdlab.infrastructure.adapters.in.web.dto.RegisterRequest;
import com.nerdlab.infrastructure.adapters.in.web.dto.UserSummaryResponse;
import com.nerdlab.infrastructure.adapters.in.web.exception.ErrorResponse;
import com.nerdlab.infrastructure.config.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para registro, login, consulta de perfil e alteração de senha")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastro de novo usuário", description = "Cria uma conta de usuário com senha criptografada em BCrypt e papel ROLE_USER")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso",
            content = @Content(schema = @Schema(implementation = UserSummaryResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "E-mail ou username já em uso",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserSummaryResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserSummaryResponse createdUser = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticação de usuário", description = "Valida credenciais e retorna um token JWT de acesso")
    @ApiResponse(responseCode = "200", description = "Autenticado com sucesso",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String principal = request.getPrincipal();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(principal, request.password())
        );

        var userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        UserSummaryResponse userSummary = userService.getProfile(userDetails.getUsername());
        String token = jwtService.generateToken(userDetails);

        AuthResponse response = AuthResponse.of(token, jwtService.getExpirationInSeconds(), userSummary);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Consulta de perfil autenticado", description = "Retorna os dados do usuário atualmente autenticado a partir do token JWT")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Perfil recuperado com sucesso",
            content = @Content(schema = @Schema(implementation = UserSummaryResponse.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT não fornecido ou inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserSummaryResponse> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication.getName();
        UserSummaryResponse profile = userService.getProfile(identifier);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/change-password")
    @Operation(summary = "Alteração de senha", description = "Permite que o usuário autenticado altere sua própria senha")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Senha não atende aos requisitos mínimos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Não autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication.getName();
        userService.updatePassword(identifier, request.newPassword());
        return ResponseEntity.ok().build();
    }
}
