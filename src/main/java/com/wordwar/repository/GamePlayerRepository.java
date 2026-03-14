package com.wordwar.repository;

import com.wordwar.entity.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    List<GamePlayer> findByGameId(Long gameId);
    Optional<GamePlayer> findByGameIdAndUserId(Long gameId, Long userId);
    boolean existsByGameIdAndUserId(Long gameId, Long userId);
    int countByGameId(Long gameId);
}
