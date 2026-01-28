package com.framework.game.session.service.impl;

import com.framework.common.dto.GameStateResponse;
import com.framework.common.dto.GameStatus;
import com.framework.common.dto.MoveRequest;
import com.framework.common.dto.SessionResponse;
import com.framework.game.session.db.entity.SessionEntity;
import com.framework.game.session.db.repository.SessionRepository;
import com.framework.game.session.service.GameSessionService;
import com.framework.game.session.exception.GameAlreadyFinishedException;
import com.framework.game.session.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameSessionServiceImpl implements GameSessionService {
    private final RestTemplate restTemplate;
    private final SessionRepository sessionRepository;

    @Value("${services.game-engine.url}")
    private String engineUrl;

    public SessionResponse createSession() {
        UUID sessionId = UUID.randomUUID();
        // Use sessionId as gameId just to simplify

        SessionEntity session = SessionEntity.builder()
            .sessionId(sessionId)
            .gameId(sessionId)
            .lastStatus(GameStatus.IN_PROGRESS)
            .moveHistory(new ArrayList<>())
            .build();

        return toResponse(sessionRepository.save(session));
    }

    public SessionResponse getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
    }

    private SessionResponse toResponse(SessionEntity session) {
        return SessionResponse.builder()
            .sessionId(session.getSessionId())
            .gameId(session.getGameId())
            .lastStatus(session.getLastStatus())
            .moveHistory(new ArrayList<>(session.getMoveHistory()))
            .createdAt(session.getCreatedAt())
            .build();
    }

    public void simulateGame(UUID sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        if (session.getLastStatus() != GameStatus.IN_PROGRESS) {
            throw new GameAlreadyFinishedException("Cannot simulate a game that is already finished");
        }

        String currentSymbol = "X";

        while (session.getLastStatus() == GameStatus.IN_PROGRESS) {
            // 1. Get current state of board from Engine
            GameStateResponse state;
            try {
                state = restTemplate.getForObject(
                    engineUrl + "/games/" + session.getGameId(), GameStateResponse.class);
            } catch (Exception e) {
                // If there is no game yet, create an empty structure for the first move
                state = GameStateResponse.builder()
                    .gameId(session.getGameId())
                    .board(new String[3][3])
                    .status(GameStatus.IN_PROGRESS)
                    .build();
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        state.getBoard()[i][j] = "-";
                    }
                }
            }

            // 2. Generate random move
            MoveRequest move = generateRandomMove(state.getBoard(), currentSymbol);
            if (move == null) break; // No moves available

            // 3. Sending move to Engine
            GameStateResponse result = restTemplate.postForObject(
                engineUrl + "/games/" + session.getGameId() + "/move", move, GameStateResponse.class);

            // 4. Refreshing the session
            session.setLastStatus(result.getStatus());
            session.getMoveHistory().add(currentSymbol + " moved to [" + move.getRow() + "," + move.getCol() + "]");

            // We save the intermediate state
            sessionRepository.save(session);

            if (result.getStatus() != GameStatus.IN_PROGRESS) break;

            // Change player
            currentSymbol = currentSymbol.equals("X") ? "O" : "X";

            // Just small pose for comfortable watching
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    private MoveRequest generateRandomMove(String[][] board, String symbol) {
        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == null || board[r][c].equals("-")) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }
        if (emptyCells.isEmpty()) return null;
        int[] choice = emptyCells.get(new Random().nextInt(emptyCells.size()));
        return new MoveRequest(symbol, choice[0], choice[1]);
    }
}
