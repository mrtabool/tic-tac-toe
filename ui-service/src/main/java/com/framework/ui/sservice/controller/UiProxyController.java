package com.framework.ui.sservice.controller;

import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UiProxyController {

    private final RestTemplate restTemplate;

    @Value("${services.game-session.url}")
    private String sessionUrl;

    @Value("${services.game-engine.url}")
    private String engineUrl;

    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> createSession() {
        return restTemplate.postForEntity(sessionUrl + "/sessions", null, SessionResponse.class);
    }

    @PostMapping("/sessions/{sessionId}/simulate")
    public ResponseEntity<Void> simulate(@PathVariable UUID sessionId) {
        restTemplate.postForEntity(sessionUrl + "/sessions/" + sessionId + "/simulate", null, Void.class);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable UUID sessionId) {
        return restTemplate.getForEntity(sessionUrl + "/sessions/" + sessionId, SessionResponse.class);
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(@PathVariable UUID gameId) {
        return restTemplate.getForEntity(engineUrl + "/games/" + gameId, GameStateResponse.class);
    }
}
