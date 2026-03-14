package com.wordwar.repository;

import com.wordwar.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MoveRepository extends JpaRepository<Move, Long> {
    List<Move> findByGameIdAndRound(Long gameId, int round);
    List<Move> findByGameId(Long gameId);
    boolean existsByGameIdAndPlayerIdAndWordAndRound(Long gameId, Long playerId, String word, int round);

    @Query("SELECT m.player.username, SUM(m.pointsAwarded) as total FROM Move m " +
           "WHERE m.valid = true GROUP BY m.player.username ORDER BY total DESC")
    List<Object[]> findGlobalLeaderboard();

    @Query("SELECT m FROM Move m WHERE m.game.id = :gid AND m.valid = true ORDER BY m.pointsAwarded DESC")
    List<Move> findTopMovesInGame(@Param("gid") Long gameId);
}
