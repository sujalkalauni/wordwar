package com.wordwar.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "moves")
@Data
@NoArgsConstructor
public class Move {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Column(nullable = false) private String word;
    private boolean valid = false;
    private int pointsAwarded = 0;
    private int round = 1;
    private LocalDateTime submittedAt = LocalDateTime.now();
}
