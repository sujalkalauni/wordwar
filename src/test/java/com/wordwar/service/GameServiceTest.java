package com.wordwar.service;

import com.wordwar.dto.Dtos.*;
import com.wordwar.engine.WordScoringEngine;
import com.wordwar.entity.*;
import com.wordwar.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock private GameRepository gameRepository;
    @Mock private GamePlayerRepository gamePlayerRepository;
    @Mock private MoveRepository moveRepository;
    @Mock private UserRepository userRepository;
    @Mock private WordScoringEngine scoringEngine;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks private GameService gameService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("lexmaster");
        testUser.setTotalScore(100);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lexmaster");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("lexmaster")).thenReturn(Optional.of(testUser));
    }

    @Test
    void createGame_savesGameAndAutoJoinsCreator() {
        CreateGameRequest req = new CreateGameRequest();
        req.setMode(Game.GameMode.CLASSIC);
        req.setMaxPlayers(2);
        req.setTimeLimitSeconds(60);
        req.setTotalRounds(3);

        when(scoringEngine.generateLetters(7)).thenReturn("STARONE");

        Game saved = new Game();
        saved.setId(1L);
        saved.setMode(Game.GameMode.CLASSIC);
        saved.setStatus(Game.GameStatus.WAITING);
        saved.setAvailableLetters("STARONE");
        saved.setCreator(testUser);

        when(gameRepository.save(any())).thenReturn(saved);
        when(gamePlayerRepository.save(any())).thenReturn(new GamePlayer());

        GameResponse res = gameService.createGame(req);

        assertThat(res.getMode()).isEqualTo(Game.GameMode.CLASSIC);
        assertThat(res.getStatus()).isEqualTo(Game.GameStatus.WAITING);
        assertThat(res.getAvailableLetters()).isEqualTo("STARONE");
        verify(gamePlayerRepository, times(1)).save(any());
    }

    @Test
    void joinGame_throwsWhenGameNotWaiting() {
        Game game = new Game();
        game.setId(1L);
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setCreator(testUser);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> gameService.joinGame(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not open");
    }

    @Test
    void submitMove_invalidWord_returnsZeroPoints() {
        Game game = new Game();
        game.setId(1L);
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setMode(Game.GameMode.CLASSIC);
        game.setAvailableLetters("ABCDEFG");
        game.setCurrentRound(1);
        game.setCreator(testUser);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.existsByGameIdAndUserId(1L, 1L)).thenReturn(true);
        when(moveRepository.existsByGameIdAndPlayerIdAndWordAndRound(any(), any(), any(), anyInt())).thenReturn(false);
        when(scoringEngine.isValidWord("ZZZQ", "ABCDEFG")).thenReturn(false);
        when(moveRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubmitMoveRequest req = new SubmitMoveRequest();
        req.setWord("ZZZQ");

        MoveResponse res = gameService.submitMove(1L, req);

        assertThat(res.isValid()).isFalse();
        assertThat(res.getPointsAwarded()).isEqualTo(0);
    }

    @Test
    void submitMove_validWord_awardsPoints() {
        Game game = new Game();
        game.setId(1L);
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setMode(Game.GameMode.CLASSIC);
        game.setAvailableLetters("STARREX");
        game.setCurrentRound(1);
        game.setCreator(testUser);

        GamePlayer gp = new GamePlayer();
        gp.setUser(testUser); gp.setScore(0);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.existsByGameIdAndUserId(1L, 1L)).thenReturn(true);
        when(moveRepository.existsByGameIdAndPlayerIdAndWordAndRound(any(), any(), any(), anyInt())).thenReturn(false);
        when(scoringEngine.isValidWord("STAR", "STARREX")).thenReturn(true);
        when(scoringEngine.scoreWord("STAR")).thenReturn(40);
        when(moveRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gamePlayerRepository.findByGameIdAndUserId(1L, 1L)).thenReturn(Optional.of(gp));
        when(gamePlayerRepository.save(any())).thenReturn(gp);
        when(userRepository.save(any())).thenReturn(testUser);

        SubmitMoveRequest req = new SubmitMoveRequest();
        req.setWord("STAR");

        MoveResponse res = gameService.submitMove(1L, req);

        assertThat(res.isValid()).isTrue();
        assertThat(res.getPointsAwarded()).isEqualTo(40);
    }

    @Test
    void submitMove_throwsOnDuplicateWord() {
        Game game = new Game();
        game.setId(1L);
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setMode(Game.GameMode.CLASSIC);
        game.setAvailableLetters("STARREX");
        game.setCurrentRound(1);
        game.setCreator(testUser);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.existsByGameIdAndUserId(1L, 1L)).thenReturn(true);
        when(moveRepository.existsByGameIdAndPlayerIdAndWordAndRound(any(), any(), any(), anyInt())).thenReturn(true);

        SubmitMoveRequest req = new SubmitMoveRequest();
        req.setWord("STAR");

        assertThatThrownBy(() -> gameService.submitMove(1L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already submitted");
    }
}
