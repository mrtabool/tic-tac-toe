package com.framework.game.engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.GameStatus;
import com.framework.common.dto.MoveRequest;
import com.framework.game.engine.service.GameEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameEngineController.class)
class GameEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameEngineService engineService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void move_ReturnsGameState() throws Exception {
        UUID gameId = UUID.randomUUID();
        MoveRequest move = new MoveRequest("X", 0, 0);
        GameStateResponse response = GameStateResponse.builder()
                .gameId(gameId)
                .board(new String[3][3])
                .status(GameStatus.IN_PROGRESS)
                .build();

        when(engineService.makeMove(eq(gameId), any(MoveRequest.class))).thenReturn(response);

        mockMvc.perform(post("/games/{gameId}/move", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(move)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getGame_ReturnsGameState() throws Exception {
        UUID gameId = UUID.randomUUID();
        GameStateResponse response = GameStateResponse.builder()
                .gameId(gameId)
                .board(new String[3][3])
                .status(GameStatus.IN_PROGRESS)
                .build();

        when(engineService.getGame(gameId)).thenReturn(response);

        mockMvc.perform(get("/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
