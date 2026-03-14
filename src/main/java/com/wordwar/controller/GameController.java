package com.wordwar.controller;

import com.wordwar.dto.Dtos.*;
import com.wordwar.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Create, join, and play word war games")
public class GameController {

    private final GameService gameService;

    @PostMapping
    @Operation(summary = "Create a new game")
    public ResponseEntity<GameResponse> create(@Valid @RequestBody CreateGameRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createGame(req));
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Join an open game")
    public ResponseEntity<GameResponse> join(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.joinGame(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get game state + scores")
    public ResponseEntity<GameResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGame(id));
    }

    @GetMapping("/open")
    @Operation(summary = "List all open games you can join")
    public ResponseEntity<List<GameResponse>> open() {
        return ResponseEntity.ok(gameService.getOpenGames());
    }

    @PostMapping("/{id}/moves")
    @Operation(summary = "Submit a word",
               description = "Word is validated against available letters. Points awarded based on length and rare letters.")
    public ResponseEntity<MoveResponse> submitMove(@PathVariable Long id,
                                                    @Valid @RequestBody SubmitMoveRequest req) {
        return ResponseEntity.ok(gameService.submitMove(id, req));
    }

    @PostMapping("/{id}/round/end")
    @Operation(summary = "End current round and advance (or finish the game)")
    public ResponseEntity<GameResponse> endRound(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.endRound(id));
    }

    @GetMapping("/{id}/moves")
    @Operation(summary = "Get all moves in a specific round")
    public ResponseEntity<List<MoveResponse>> getMoves(@PathVariable Long id,
                                                        @RequestParam(defaultValue = "1") int round) {
        return ResponseEntity.ok(gameService.getMovesForRound(id, round));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Global leaderboard — top players by total points")
    public ResponseEntity<List<LeaderboardEntry>> leaderboard() {
        return ResponseEntity.ok(gameService.getLeaderboard());
    }

    @GetMapping("/stats/me")
    @Operation(summary = "Your personal stats — score, win rate, games played")
    public ResponseEntity<PlayerStatsResponse> myStats() {
        return ResponseEntity.ok(gameService.myStats());
    }
}
