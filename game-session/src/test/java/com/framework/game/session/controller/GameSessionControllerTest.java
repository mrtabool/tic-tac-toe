package com.framework.game.session.controller;

import com.framework.common.dto.GameStatus;
import com.framework.common.dto.SessionResponse;
import com.framework.game.session.service.GameSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameSessionController.class)
class GameSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameSessionService sessionService;

    @Test
    void create_ReturnsSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        SessionResponse response = SessionResponse.builder()
                .sessionId(sessionId)
                .gameId(sessionId)
                .lastStatus(GameStatus.IN_PROGRESS)
                .moveHistory(new ArrayList<>())
                .build();

        when(sessionService.createSession()).thenReturn(response);

        mockMvc.perform(post("/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.lastStatus").value("IN_PROGRESS"));
    }

    @Test
    void simulate_ReturnsAccepted() throws Exception {
        UUID sessionId = UUID.randomUUID();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
                .andExpect(status().isAccepted());
    }

    @Test
    void get_ReturnsSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        SessionResponse response = SessionResponse.builder()
                .sessionId(sessionId)
                .gameId(sessionId)
                .lastStatus(GameStatus.IN_PROGRESS)
                .moveHistory(new ArrayList<>())
                .build();

        when(sessionService.getSession(sessionId)).thenReturn(response);

        mockMvc.perform(get("/sessions/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.lastStatus").value("IN_PROGRESS"));
    }
}
