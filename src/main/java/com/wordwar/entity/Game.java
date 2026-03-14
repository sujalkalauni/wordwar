package com.wordwar.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private GameMode mode = GameMode.CLASSIC;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private GameStatus status = GameStatus.WAITING;

    // 7 random letters for this game
    @Column(nullable = false)
    private String availableLetters;

    // The target word to beat (ANAGRAM mode)
    private String targetWord;

    private int maxPlayers = 2;
    private int timeLimitSeconds = 60;
    private int currentRound = 1;
    private int totalRounds = 3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    // Winner set at end of game
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public enum GameMode {
        CLASSIC,   // Longest valid word from the letters wins
        SPEED,     // First to submit a valid word wins each round
        ANAGRAM    // Unscramble the exact target word
    }

    public enum GameStatus {
        WAITING, IN_PROGRESS, COMPLETED, ABANDONED
    }
}
