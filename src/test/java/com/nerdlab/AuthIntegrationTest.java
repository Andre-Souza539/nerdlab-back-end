package com.nerdlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nerdlab.infrastructure.adapters.in.web.dto.AuthResponse;
import com.nerdlab.infrastructure.adapters.in.web.dto.LoginRequest;
import com.nerdlab.infrastructure.adapters.in.web.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("AUTH-01: Cadastro de novo usuário retorna 201 Created com dados resumidos")
    void register_validPayload_shouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Carlos Silva",
                "carlos_silva",
                "carlos.silva@example.com",
                "SenhaForte@2026"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("carlos.silva@example.com")))
                .andExpect(jsonPath("$.username", is("carlos_silva")))
                .andExpect(jsonPath("$.name", is("Carlos Silva")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));
    }

    @Test
    @DisplayName("AUTH-02: Cadastro com e-mail duplicado retorna 409 Conflict")
    void register_duplicateEmail_shouldReturnConflict() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Admin Duplicado",
                "admin_clone",
                "admin@nerdlab.com",
                "SenhaForte@2026"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("AUTH-03: Login com credenciais válidas retorna 200 OK com token JWT")
    void login_validCredentials_shouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest("admin@nerdlab.com", null, "admin123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.expiresInSeconds", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("admin@nerdlab.com")));
    }

    @Test
    @DisplayName("AUTH-04: Login com senha incorreta retorna 401 Unauthorized")
    void login_invalidPassword_shouldReturnUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("admin@nerdlab.com", null, "senhaInvalida123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-05: Consulta ao perfil /api/v1/auth/me sem token retorna 401 Unauthorized")
    void getProfile_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-06: Consulta ao perfil /api/v1/auth/me com token válido retorna 200 OK com dados do usuário")
    void getProfile_authenticated_shouldReturnProfile() throws Exception {
        // 1. Realiza login para obter o token JWT
        LoginRequest loginRequest = new LoginRequest("admin@nerdlab.com", null, "admin123456");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthResponse.class
        );

        // 2. Consulta perfil com o Bearer token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + authResponse.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("admin@nerdlab.com")))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")));
    }
}
