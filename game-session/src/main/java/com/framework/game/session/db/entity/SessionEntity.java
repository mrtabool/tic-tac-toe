package com.framework.game.session.db.entity;

import com.framework.common.dto.GameStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEntity {
    @Id
    private UUID sessionId;

    private UUID gameId;

    @Enumerated(EnumType.STRING)
    private GameStatus lastStatus;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_history", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "move_description")
    private List<String> moveHistory;

    private LocalDateTime createdAt;

    @PrePersist
    public void init() {
        if (this.sessionId == null) {
            this.sessionId = UUID.randomUUID();
        }
        if (this.moveHistory == null) {
            this.moveHistory = new ArrayList<>();
        }
        if (this.lastStatus == null) {
            this.lastStatus = GameStatus.IN_PROGRESS;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
