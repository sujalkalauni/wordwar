package com.wordwar.service;

import com.wordwar.dto.Dtos.*;
import com.wordwar.engine.WordScoringEngine;
import com.wordwar.entity.*;
import com.wordwar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final MoveRepository moveRepository;
    private final UserRepository userRepository;
    private final WordScoringEngine scoringEngine;

    @Transactional
    public GameResponse createGame(CreateGameRequest req) {
        User user = currentUser();
        String letters = scoringEngine.generateLetters(7);
        String targetWord = pickTargetWord(letters);

        Game game = new Game();
        game.setMode(req.getMode());
        game.setMaxPlayers(req.getMaxPlayers());
        game.setTimeLimitSeconds(req.getTimeLimitSeconds());
        game.setTotalRounds(req.getTotalRounds());
        game.setAvailableLetters(letters);
        game.setTargetWord(targetWord);
        game.setCreator(user);
        game = gameRepository.save(game);

        // Creator auto-joins
        GamePlayer gp = new GamePlayer();
        gp.setGame(game); gp.setUser(user); gp.setReady(false);
        gamePlayerRepository.save(gp);

        return GameResponse.from(game);
    }

    @Transactional
    public GameResponse joinGame(Long gameId) {
        User user = currentUser();
        Game game = findGame(gameId);

        if (game.getStatus() != Game.GameStatus.WAITING)
            throw new IllegalStateException("Game is not open for joining");
        if (gamePlayerRepository.existsByGameIdAndUserId(gameId, user.getId()))
            throw new IllegalStateException("You already joined this game");
        if (gamePlayerRepository.countByGameId(gameId) >= game.getMaxPlayers())
            throw new IllegalStateException("Game is full");

        GamePlayer gp = new GamePlayer();
        gp.setGame(game); gp.setUser(user);
        gamePlayerRepository.save(gp);

        // Auto-start if full
        if (gamePlayerRepository.countByGameId(gameId) >= game.getMaxPlayers()) {
            game.setStatus(Game.GameStatus.IN_PROGRESS);
            game.setStartedAt(LocalDateTime.now());
            gameRepository.save(game);
        }

        return buildGameResponse(game);
    }

    @Transactional
    public MoveResponse submitMove(Long gameId, SubmitMoveRequest req) {
        User user = currentUser();
        Game game = findGame(gameId);

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS)
            throw new IllegalStateException("Game is not in progress");
        if (!gamePlayerRepository.existsByGameIdAndUserId(gameId, user.getId()))
            throw new IllegalStateException("You are not in this game");
        if (moveRepository.existsByGameIdAndPlayerIdAndWordAndRound(gameId, user.getId(), req.getWord().toUpperCase(), game.getCurrentRound()))
            throw new IllegalStateException("You already submitted this word this round");

        String word = req.getWord().toUpperCase();
        boolean valid = false;
        int points = 0;
        String feedback;

        if (game.getMode() == Game.GameMode.ANAGRAM) {
            valid = scoringEngine.isCorrectAnagram(word, game.getTargetWord());
            feedback = valid ? "Correct! You unscrambled it!" : "Wrong — that's not the target word.";
        } else {
            valid = scoringEngine.isValidWord(word, game.getAvailableLetters());
            feedback = valid ? "Valid word! +" + scoringEngine.scoreWord(word) + " points" : "Invalid — word not found or letters don't match.";
        }

        if (valid) points = scoringEngine.scoreWord(word);

        Move move = new Move();
        move.setGame(game); move.setPlayer(user);
        move.setWord(word); move.setValid(valid);
        move.setPointsAwarded(points);
        move.setRound(game.getCurrentRound());
        moveRepository.save(move);

        // Update player score
        if (valid) {
            GamePlayer gp = gamePlayerRepository.findByGameIdAndUserId(gameId, user.getId())
                    .orElseThrow();
            gp.setScore(gp.getScore() + points);
            gamePlayerRepository.save(gp);

            user.setTotalScore(user.getTotalScore() + points);
            userRepository.save(user);
        }

        return MoveResponse.from(move, feedback);
    }

    @Transactional
    public GameResponse endRound(Long gameId) {
        Game game = findGame(gameId);
        if (game.getStatus() != Game.GameStatus.IN_PROGRESS)
            throw new IllegalStateException("Game is not in progress");

        if (game.getCurrentRound() >= game.getTotalRounds()) {
            // Game over — find winner
            List<GamePlayer> players = gamePlayerRepository.findByGameId(gameId);
            GamePlayer winner = players.stream()
                    .max((a, b) -> Integer.compare(a.getScore(), b.getScore()))
                    .orElseThrow();

            game.setStatus(Game.GameStatus.COMPLETED);
            game.setEndedAt(LocalDateTime.now());
            game.setWinner(winner.getUser());

            // Update stats
            winner.getUser().setGamesWon(winner.getUser().getGamesWon() + 1);
            players.forEach(p -> {
                p.getUser().setGamesPlayed(p.getUser().getGamesPlayed() + 1);
                userRepository.save(p.getUser());
            });
        } else {
            game.setCurrentRound(game.getCurrentRound() + 1);
            game.setAvailableLetters(scoringEngine.generateLetters(7));
        }

        gameRepository.save(game);
        return buildGameResponse(game);
    }

    public List<GameResponse> getOpenGames() {
        User user = currentUser();
        return gameRepository.findJoinableGames(user.getId())
                .stream().map(GameResponse::from).collect(Collectors.toList());
    }

    public GameResponse getGame(Long gameId) {
        return buildGameResponse(findGame(gameId));
    }

    public List<MoveResponse> getMovesForRound(Long gameId, int round) {
        return moveRepository.findByGameIdAndRound(gameId, round)
                .stream().map(m -> MoveResponse.from(m, null)).collect(Collectors.toList());
    }

    public List<LeaderboardEntry> getLeaderboard() {
        List<Object[]> results = moveRepository.findGlobalLeaderboard();
        List<LeaderboardEntry> board = new java.util.ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            board.add(new LeaderboardEntry(i + 1, (String) row[0], ((Number) row[1]).longValue()));
        }
        return board;
    }

    public PlayerStatsResponse myStats() {
        User user = currentUser();
        PlayerStatsResponse stats = new PlayerStatsResponse();
        stats.setUsername(user.getUsername());
        stats.setTotalScore(user.getTotalScore());
        stats.setGamesPlayed(user.getGamesPlayed());
        stats.setGamesWon(user.getGamesWon());
        stats.setWinRate(user.getGamesPlayed() > 0
                ? (double) user.getGamesWon() / user.getGamesPlayed() * 100 : 0);
        return stats;
    }

    // ── Helpers ──────────────────────────────────────────

    private GameResponse buildGameResponse(Game game) {
        GameResponse r = GameResponse.from(game);
        List<GamePlayer> players = gamePlayerRepository.findByGameId(game.getId());
        r.setScores(players.stream()
                .map(gp -> new PlayerScoreDto(gp.getUser().getUsername(), gp.getScore()))
                .collect(Collectors.toList()));
        return r;
    }

    private Game findGame(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + id));
    }

    private String pickTargetWord(String letters) {
        // Simple: return the letters scrambled as the "target" for ANAGRAM mode
        List<Character> chars = new java.util.ArrayList<>();
        for (char c : letters.toCharArray()) chars.add(c);
        java.util.Collections.shuffle(chars);
        StringBuilder sb = new StringBuilder();
        chars.forEach(sb::append);
        return sb.toString();
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
