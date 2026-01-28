package com.framework.game.session.service;

import com.framework.common.dto.SessionResponse;
import java.util.UUID;

public interface GameSessionService {
    SessionResponse createSession();
    SessionResponse getSession(UUID sessionId);
    void simulateGame(UUID sessionId);
}
