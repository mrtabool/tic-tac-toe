package com.framework.game.session.controller;

import com.framework.common.dto.SessionResponse;
import com.framework.game.session.service.GameSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class GameSessionController {
    private final GameSessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> create() {
        return ResponseEntity.ok(sessionService.createSession());
    }

    @PostMapping("/{sessionId}/simulate")
    public ResponseEntity<Void> simulate(@PathVariable UUID sessionId) {
        CompletableFuture.runAsync(() -> sessionService.simulateGame(sessionId));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> get(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(sessionService.getSession(sessionId));
    }
}
