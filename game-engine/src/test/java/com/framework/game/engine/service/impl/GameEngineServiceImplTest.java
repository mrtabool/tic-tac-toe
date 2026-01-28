package com.framework.game.engine.service.impl;

import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.GameStatus;
import com.framework.common.dto.MoveRequest;
import com.framework.game.engine.db.entity.GameEntity;
import com.framework.game.engine.db.repository.GameRepository;
import com.framework.game.engine.exception.InvalidMoveException;
import com.framework.game.engine.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameEngineServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameEngineServiceImpl gameEngineService;

    private UUID gameId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
    }

    @Test
    void makeMove_NewGame_Success() {
        GameEntity game = new GameEntity();
        game.setId(gameId);
        game.setBoard("---------");
        game.setStatus(GameStatus.IN_PROGRESS);

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
        when(gameRepository.save(any(GameEntity.class))).thenReturn(game);

        MoveRequest move = new MoveRequest("X", 0, 0);
        GameStateResponse response = gameEngineService.makeMove(gameId, move);

        assertNotNull(response);
        assertEquals("X", response.getBoard()[0][0]);
        assertEquals(GameStatus.IN_PROGRESS, response.getStatus());
        verify(gameRepository, times(2)).save(any(GameEntity.class));
    }

    @Test
    void makeMove_CellOccupied_ThrowsException() {
        GameEntity game = new GameEntity();
        game.setId(gameId);
        game.setBoard("X--------");
        game.setStatus(GameStatus.IN_PROGRESS);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        MoveRequest move = new MoveRequest("O", 0, 0);
        assertThrows(InvalidMoveException.class, () -> gameEngineService.makeMove(gameId, move));
    }

    @Test
    void makeMove_GameFinished_ThrowsException() {
        GameEntity game = new GameEntity();
        game.setId(gameId);
        game.setBoard("XXXOO----");
        game.setStatus(GameStatus.X_WINS);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        MoveRequest move = new MoveRequest("O", 2, 0);
        assertThrows(InvalidMoveException.class, () -> gameEngineService.makeMove(gameId, move));
    }

    @Test
    void makeMove_InvalidPosition_ThrowsException() {
        GameEntity game = new GameEntity();
        game.setId(gameId);
        game.setBoard("---------");
        game.setStatus(GameStatus.IN_PROGRESS);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        MoveRequest move = new MoveRequest("X", 3, 0);
        assertThrows(InvalidMoveException.class, () -> gameEngineService.makeMove(gameId, move));
    }

    @Test
    void getGame_Exists_ReturnsResponse() {
        GameEntity game = new GameEntity();
        game.setId(gameId);
        game.setBoard("X--------");
        game.setStatus(GameStatus.IN_PROGRESS);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        GameStateResponse response = gameEngineService.getGame(gameId);
        assertNotNull(response);
        assertEquals("X", response.getBoard()[0][0]);
    }

    @Test
    void getGame_NotExists_ThrowsException() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> gameEngineService.getGame(gameId));
    }

    @Test
    void checkStatus_XWins_ReturnsXWins() {
        String board = "XXXOO----";
        assertEquals(GameStatus.X_WINS, gameEngineService.checkStatus(board));
    }

    @Test
    void checkStatus_OWins_ReturnsOWins() {
        String board = "OOOXX----";
        assertEquals(GameStatus.O_WINS, gameEngineService.checkStatus(board));
    }

    @Test
    void checkStatus_Draw_ReturnsDraw() {
        String board = "XOXOOXXXO";
        assertEquals(GameStatus.DRAW, gameEngineService.checkStatus(board));
    }

    @Test
    void checkStatus_InProgress_ReturnsInProgress() {
        String board = "X--------";
        assertEquals(GameStatus.IN_PROGRESS, gameEngineService.checkStatus(board));
    }
}
