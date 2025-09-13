package com.example.controller;

import com.example.model.Player;
import com.example.model.PlayerStats;
import com.example.service.PlayerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    
    private final PlayerService playerService;
    
    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    
    // Create a new player
    @PostMapping
    public ResponseEntity<Player> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        try {
            Player player = playerService.createPlayer(request.getName(), request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get player by ID
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable String id) {
        return playerService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Get all players
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers(
            @RequestParam(required = false) String name) {
        List<Player> players = playerService.searchByName(name);
        return ResponseEntity.ok(players);
    }
    
    // Update player
    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(
            @PathVariable String id,
            @Valid @RequestBody UpdatePlayerRequest request) {
        try {
            Player player = playerService.updatePlayer(id, request.getName(), request.getEmail());
            return ResponseEntity.ok(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Delete player
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        boolean deleted = playerService.deletePlayer(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // Get player statistics
    @GetMapping("/{id}/stats")
    public ResponseEntity<PlayerStats> getPlayerStats(@PathVariable String id) {
        try {
            PlayerStats stats = playerService.getPlayerStats(id);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get leaderboard
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Player>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        List<Player> leaderboard = playerService.getLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }
    
    // Clear all players (for testing purposes)
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAllPlayers() {
        playerService.clearAllPlayers();
        return ResponseEntity.ok().build();
    }
    
    // Get most active players
    @GetMapping("/most-active")
    public ResponseEntity<List<Player>> getMostActivePlayers(
            @RequestParam(defaultValue = "10") int limit) {
        List<Player> players = playerService.getMostActivePlayers(limit);
        return ResponseEntity.ok(players);
    }
    
    // Get most efficient players
    @GetMapping("/most-efficient")
    public ResponseEntity<List<Player>> getMostEfficientPlayers(
            @RequestParam(defaultValue = "10") int limit) {
        List<Player> players = playerService.getMostEfficientPlayers(limit);
        return ResponseEntity.ok(players);
    }
    
    // Get player count
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getPlayerCount() {
        long count = playerService.getTotalPlayerCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    // Request/Response DTOs
    public static class CreatePlayerRequest {
        @NotBlank(message = "Player name is required")
        @Size(min = 1, max = 100, message = "Player name must be between 1 and 100 characters")
        private String name;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    public static class UpdatePlayerRequest {
        @NotBlank(message = "Player name is required")
        @Size(min = 1, max = 100, message = "Player name must be between 1 and 100 characters")
        private String name;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
}
