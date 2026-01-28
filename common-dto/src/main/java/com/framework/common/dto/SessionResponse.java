package com.framework.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {
    private UUID sessionId;
    private UUID gameId;
    private GameStatus lastStatus;
    private List<String> moveHistory;
    private LocalDateTime createdAt;
}
