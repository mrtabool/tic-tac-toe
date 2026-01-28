package com.framework.game.engine.service;

import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.GameStatus;
import com.framework.common.dto.MoveRequest;

import java.util.UUID;

public interface GameEngineService {
    GameStateResponse makeMove(UUID gameId, MoveRequest move);
    GameStateResponse getGame(UUID gameId);
    GameStatus checkStatus(String board);
}
