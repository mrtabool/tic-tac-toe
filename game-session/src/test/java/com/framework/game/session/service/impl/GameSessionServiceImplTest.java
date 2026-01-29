package com.framework.game.session.service.impl;

import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.GameStatus;
import com.framework.common.dto.MoveRequest;
import com.framework.common.dto.SessionResponse;
import com.framework.game.session.db.entity.SessionEntity;
import com.framework.game.session.db.repository.SessionRepository;
import com.framework.game.session.exception.GameAlreadyFinishedException;
import com.framework.game.session.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameSessionServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private GameSessionServiceImpl sessionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sessionService, "engineUrl", "http://localhost:8081");
    }

    @Test
    void createSession_Success() {
        when(sessionRepository.save(any(SessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.createSession();

        assertNotNull(response);
        assertNotNull(response.getSessionId());
        assertEquals(GameStatus.IN_PROGRESS, response.getLastStatus());
        verify(sessionRepository).save(any(SessionEntity.class));
    }

    @Test
    void getSession_Success() {
        UUID sessionId = UUID.randomUUID();
        SessionEntity entity = SessionEntity.builder()
                .sessionId(sessionId)
                .gameId(sessionId)
                .lastStatus(GameStatus.IN_PROGRESS)
                .moveHistory(new ArrayList<>())
                .build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(entity));

        SessionResponse response = sessionService.getSession(sessionId);

        assertEquals(sessionId, response.getSessionId());
        verify(sessionRepository).findById(sessionId);
    }

    @Test
    void getSession_NotFound_ThrowsException() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sessionService.getSession(sessionId));
    }

    @Test
    void simulateGame_AlreadyFinished_ThrowsException() {
        UUID sessionId = UUID.randomUUID();
        SessionEntity entity = SessionEntity.builder()
                .sessionId(sessionId)
                .lastStatus(GameStatus.X_WINS)
                .build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(entity));

        assertThrows(GameAlreadyFinishedException.class, () -> sessionService.simulateGame(sessionId));
    }

    @Test
    void simulateGame_FullSimulation_Success() {
        UUID sessionId = UUID.randomUUID();
        SessionEntity entity = SessionEntity.builder()
                .sessionId(sessionId)
                .gameId(sessionId)
                .lastStatus(GameStatus.IN_PROGRESS)
                .moveHistory(new ArrayList<>())
                .build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(entity));

        GameStateResponse stateBefore = GameStateResponse.builder()
                .gameId(sessionId)
                .board(new String[][]{{"-","-","-"},{"-","-","-"},{"-","-","-"}})
                .status(GameStatus.IN_PROGRESS)
                .build();

        GameStateResponse stateAfter = GameStateResponse.builder()
                .gameId(sessionId)
                .board(new String[][]{{"X","-","-"},{"-","-","-"},{"-","-","-"}})
                .status(GameStatus.X_WINS)
                .build();

        when(restTemplate.getForObject(anyString(), eq(GameStateResponse.class))).thenReturn(stateBefore);
        when(restTemplate.postForObject(anyString(), any(MoveRequest.class), eq(GameStateResponse.class))).thenReturn(stateAfter);
        when(sessionRepository.save(any(SessionEntity.class))).thenReturn(entity);

        sessionService.simulateGame(sessionId);

        assertEquals(GameStatus.X_WINS, entity.getLastStatus());
        assertFalse(entity.getMoveHistory().isEmpty());
        verify(sessionRepository, atLeastOnce()).save(any(SessionEntity.class));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), any(SessionResponse.class));
    }
}
