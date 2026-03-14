package com.wordwar.dto;

import com.wordwar.entity.Game;
import com.wordwar.entity.Move;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class Dtos {

    // ── Auth ──────────────────────────────────────────────

    @Data
    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank @Email private String email;
        @NotBlank private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String username;
        private int totalScore;
        public AuthResponse(String token, String username, int totalScore) {
            this.token = token; this.username = username; this.totalScore = totalScore;
        }
    }

    // ── Game ──────────────────────────────────────────────

    @Data
    public static class CreateGameRequest {
        @NotNull private Game.GameMode mode;
        private int maxPlayers = 2;
        private int timeLimitSeconds = 60;
        private int totalRounds = 3;
    }

    @Data
    public static class GameResponse {
        private Long id;
        private Game.GameMode mode;
        private Game.GameStatus status;
        private String availableLetters;
        private int maxPlayers;
        private int currentRound;
        private int totalRounds;
        private int timeLimitSeconds;
        private String creatorUsername;
        private String winnerUsername;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private List<PlayerScoreDto> scores;

        public static GameResponse from(com.wordwar.entity.Game g) {
            GameResponse r = new GameResponse();
            r.id = g.getId();
            r.mode = g.getMode();
            r.status = g.getStatus();
            r.availableLetters = g.getAvailableLetters();
            r.maxPlayers = g.getMaxPlayers();
            r.currentRound = g.getCurrentRound();
            r.totalRounds = g.getTotalRounds();
            r.timeLimitSeconds = g.getTimeLimitSeconds();
            r.creatorUsername = g.getCreator() != null ? g.getCreator().getUsername() : null;
            r.winnerUsername = g.getWinner() != null ? g.getWinner().getUsername() : null;
            r.createdAt = g.getCreatedAt();
            r.startedAt = g.getStartedAt();
            r.endedAt = g.getEndedAt();
            return r;
        }
    }

    // ── Move ──────────────────────────────────────────────

    @Data
    public static class SubmitMoveRequest {
        @NotBlank private String word;
    }

    @Data
    public static class MoveResponse {
        private Long id;
        private String word;
        private boolean valid;
        private int pointsAwarded;
        private int round;
        private String playerUsername;
        private LocalDateTime submittedAt;
        private String feedback;

        public static MoveResponse from(Move m, String feedback) {
            MoveResponse r = new MoveResponse();
            r.id = m.getId();
            r.word = m.getWord();
            r.valid = m.isValid();
            r.pointsAwarded = m.getPointsAwarded();
            r.round = m.getRound();
            r.playerUsername = m.getPlayer() != null ? m.getPlayer().getUsername() : null;
            r.submittedAt = m.getSubmittedAt();
            r.feedback = feedback;
            return r;
        }
    }

    // ── Leaderboard ───────────────────────────────────────

    @Data
    public static class LeaderboardEntry {
        private int rank;
        private String username;
        private long totalPoints;
        public LeaderboardEntry(int rank, String username, long totalPoints) {
            this.rank = rank; this.username = username; this.totalPoints = totalPoints;
        }
    }

    @Data
    public static class PlayerScoreDto {
        private String username;
        private int score;
        public PlayerScoreDto(String username, int score) {
            this.username = username; this.score = score;
        }
    }

    @Data
    public static class PlayerStatsResponse {
        private String username;
        private int totalScore;
        private int gamesPlayed;
        private int gamesWon;
        private double winRate;
    }
}
