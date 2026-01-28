package com.framework.game.engine.controller;

import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.MoveRequest;
import com.framework.game.engine.service.GameEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameEngineController {
    private final GameEngineService engineService;

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameStateResponse> move(@PathVariable UUID gameId, @RequestBody MoveRequest move) {
        return ResponseEntity.ok(engineService.makeMove(gameId, move));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(engineService.getGame(gameId));
    }
}
