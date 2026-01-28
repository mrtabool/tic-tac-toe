package com.framework.game.engine.db.entity;

import com.framework.common.dto.GameStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEntity {

    @Id
    private UUID id;

    @Column(length = 9)
    private String board;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @PrePersist
    public void init() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.board == null) this.board = "---------";
        if (this.status == null) this.status = GameStatus.IN_PROGRESS;
    }
}
