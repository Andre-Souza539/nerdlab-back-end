package com.nerdlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("PING-01: Chamada anônima a /api/v1/ping deve retornar 200 OK com mensagem 'pong'")
    void ping_anonymousAccess_shouldReturnPong() throws Exception {
        mockMvc.perform(get("/api/v1/ping")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("pong")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.authenticatedUser").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin@nerdlab.com", roles = {"ADMIN"})
    @DisplayName("PING-02: Chamada autenticada a /api/v1/ping deve retornar 200 OK com dados do usuário")
    void ping_authenticatedAccess_shouldReturnPongWithUser() throws Exception {
        mockMvc.perform(get("/api/v1/ping")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("pong")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
