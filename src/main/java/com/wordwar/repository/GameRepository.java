package com.wordwar.repository;

import com.wordwar.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStatus(Game.GameStatus status);

    @Query("SELECT g FROM Game g WHERE g.status = 'WAITING' AND g.creator.id != :userId")
    List<Game> findJoinableGames(@Param("userId") Long userId);

    @Query("SELECT g FROM Game g JOIN GamePlayer gp ON gp.game = g WHERE gp.user.id = :userId")
    List<Game> findGamesByPlayer(@Param("userId") Long userId);
}
