package com.example.repository;

import com.example.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {
    
    // Find games by status
    List<Game> findByStatus(Game.GameStatus status);
    
    // Find active games
    List<Game> findByStatus(Game.GameStatus.ACTIVE);
    
    // Find waiting games
    List<Game> findByStatus(Game.GameStatus.WAITING);
    
    // Find completed games
    List<Game> findByStatusIn(List.of(Game.GameStatus.COMPLETED, Game.GameStatus.DRAW));
    
    // Find games by player ID
    @Query("SELECT g FROM Game g JOIN g.players p WHERE p.id = :playerId")
    List<Game> findGamesByPlayerId(@Param("playerId") String playerId);
    
    // Find games by current player
    List<Game> findByCurrentPlayerId(String currentPlayerId);
    
    // Find games by winner
    List<Game> findByWinnerId(String winnerId);
    
    // Find games created in date range
    List<Game> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find games updated in date range
    List<Game> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find games with most moves
    @Query("SELECT g FROM Game g ORDER BY SIZE(g.moves) DESC")
    List<Game> findGamesByMoveCount();
    
    // Find games by name (case-insensitive)
    List<Game> findByNameContainingIgnoreCase(String name);
    
    // Count games by status
    long countByStatus(Game.GameStatus status);
    
    // Count active games
    long countByStatus(Game.GameStatus.ACTIVE);
    
    // Count waiting games
    long countByStatus(Game.GameStatus.WAITING);
    
    // Find recent games
    List<Game> findTop10ByOrderByCreatedAtDesc();
    
    // Find games by player count
    @Query("SELECT g FROM Game g WHERE SIZE(g.players) = :playerCount")
    List<Game> findGamesByPlayerCount(@Param("playerCount") int playerCount);
    
    // Find games with specific player
    @Query("SELECT g FROM Game g JOIN g.players p WHERE p.id = :playerId AND g.status = :status")
    List<Game> findGamesByPlayerAndStatus(@Param("playerId") String playerId, @Param("status") Game.GameStatus status);
}

// TODO: Add Player and Game model input validation [ttt.todo.model.validation]