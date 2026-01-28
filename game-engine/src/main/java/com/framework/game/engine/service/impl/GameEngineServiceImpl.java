package com.framework.game.engine.service.impl;

import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.GameStatus;
import com.framework.common.dto.MoveRequest;
import com.framework.game.engine.db.entity.GameEntity;
import com.framework.game.engine.db.repository.GameRepository;
import com.framework.game.engine.service.GameEngineService;
import com.framework.game.engine.exception.InvalidMoveException;
import com.framework.game.engine.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameEngineServiceImpl implements GameEngineService {

    private final GameRepository gameRepository;

    @Transactional
    public GameStateResponse makeMove(UUID gameId, MoveRequest move) {
        GameEntity game = gameRepository.findById(gameId)
            .orElseGet(() -> {
                GameEntity newGame = new GameEntity();
                newGame.setId(gameId);
                return gameRepository.save(newGame);
            });

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("Game is already finished");
        }

        int index = move.getRow() * 3 + move.getCol();
        if (index < 0 || index >= 9) {
            throw new InvalidMoveException("Invalid board position: [" + move.getRow() + "," + move.getCol() + "]");
        }

        char[] boardChars = game.getBoard().toCharArray();

        if (boardChars[index] != '-') {
            throw new InvalidMoveException("Cell [" + move.getRow() + "," + move.getCol() + "] is already occupied");
        }

        boardChars[index] = move.getPlayerSymbol().charAt(0);
        game.setBoard(new String(boardChars));
        game.setStatus(checkStatus(game.getBoard()));
        game = gameRepository.save(game);

        return toResponse(game);
    }

    public GameStateResponse getGame(UUID gameId) {
        GameEntity game = gameRepository.findById(gameId)
            .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
        return toResponse(game);
    }

    private GameStateResponse toResponse(GameEntity game) {
        String[][] board = new String[3][3];
        for (int i = 0; i < 9; i++) {
            board[i / 3][i % 3] = String.valueOf(game.getBoard().charAt(i));
        }
        return GameStateResponse.builder()
            .gameId(game.getId())
            .board(board)
            .status(game.getStatus())
            .build();
    }

    public GameStatus checkStatus(String board) {
        int[][] winPatterns = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
        };

        for (int[] p : winPatterns) {
            if (board.charAt(p[0]) != '-' &&
                board.charAt(p[0]) == board.charAt(p[1]) &&
                board.charAt(p[0]) == board.charAt(p[2])) {
                return board.charAt(p[0]) == 'X' ? GameStatus.X_WINS : GameStatus.O_WINS;
            }
        }

        if (!board.contains("-")) return GameStatus.DRAW;
        return GameStatus.IN_PROGRESS;
    }
}
