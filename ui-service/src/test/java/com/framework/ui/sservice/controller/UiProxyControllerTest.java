package com.framework.ui.sservice.controller;

import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.GameStatus;
import com.framework.common.dto.SessionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UiProxyController.class)
class UiProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void createSession_ReturnsSessionResponse() throws Exception {
        UUID sessionId = UUID.randomUUID();
        SessionResponse response = SessionResponse.builder()
            .sessionId(sessionId)
            .gameId(sessionId)
            .lastStatus(GameStatus.IN_PROGRESS)
            .moveHistory(new ArrayList<>())
            .build();

        when(restTemplate.postForEntity(anyString(), eq(null), eq(SessionResponse.class)))
            .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/api/sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
            .andExpect(jsonPath("$.lastStatus").value("IN_PROGRESS"));
    }

    @Test
    void simulate_ReturnsOk() throws Exception {
        UUID sessionId = UUID.randomUUID();

        when(restTemplate.postForEntity(anyString(), eq(null), eq(Void.class)))
            .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/api/sessions/{sessionId}/simulate", sessionId))
            .andExpect(status().isOk());
    }

    @Test
    void getSession_ReturnsSessionResponse() throws Exception {
        UUID sessionId = UUID.randomUUID();
        SessionResponse response = SessionResponse.builder()
            .sessionId(sessionId)
            .gameId(sessionId)
            .lastStatus(GameStatus.IN_PROGRESS)
            .moveHistory(new ArrayList<>())
            .build();

        when(restTemplate.getForEntity(anyString(), eq(SessionResponse.class)))
            .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/sessions/{sessionId}", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value(sessionId.toString()));
    }

    @Test
    void getGame_ReturnsGameStateResponse() throws Exception {
        UUID gameId = UUID.randomUUID();
        GameStateResponse response = GameStateResponse.builder()
            .gameId(gameId)
            .board(new String[3][3])
            .status(GameStatus.IN_PROGRESS)
            .build();

        when(restTemplate.getForEntity(anyString(), eq(GameStateResponse.class)))
            .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/games/{gameId}", gameId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    void handleRestClientException_ProxiesError() throws Exception {
        UUID sessionId = UUID.randomUUID();
        String errorJson = "{\"message\":\"Session not found\",\"status\":404}";

        when(restTemplate.getForEntity(anyString(), eq(SessionResponse.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found",
                errorJson.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/sessions/{sessionId}", sessionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Session not found"));
    }
}
