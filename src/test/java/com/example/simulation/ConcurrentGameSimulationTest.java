package com.example.simulation;

import com.example.model.Game;
import com.example.model.Player;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.profiles.active=test"})
public class ConcurrentGameSimulationTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Test
    void simulateConcurrentGameSessions() throws Exception {
        restTemplate = new TestRestTemplate();
        objectMapper = new ObjectMapper();
        
        System.out.println("🎮 Starting Concurrent Game Simulation...");
        System.out.println("=" .repeat(60));
        
        // Clear existing data
        clearAllData();
        
        // Create players
        List<Player> players = createPlayers(6);
        System.out.println("Created " + players.size() + " players");
        
        // Simulate concurrent game sessions
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<GameResult>> futures = new ArrayList<>();
        
        // Create multiple game sessions concurrently
        for (int i = 0; i < 10; i++) {
            final int gameNumber = i + 1;
            Future<GameResult> future = executor.submit(() -> simulateGameSession(gameNumber, players));
            futures.add(future);
        }
        
        // Wait for all games to complete and collect results
        List<GameResult> results = new ArrayList<>();
        for (Future<GameResult> future : futures) {
            try {
                GameResult result = future.get(30, TimeUnit.SECONDS);
                results.add(result);
                System.out.println("Game " + result.gameNumber + " completed: " + 
                    result.winner + " vs " + result.loser + " (" + result.moves + " moves)");
            } catch (TimeoutException e) {
                System.err.println("Game timed out");
            }
        }
        
        executor.shutdown();
        
        System.out.println("\nSIMULATION RESULTS");
        System.out.println("=" .repeat(60));
        System.out.println("Total games played: " + results.size());
        
        // Show leaderboard by wins
        showLeaderboard("WINS", "wins");
        
        // Show leaderboard by win rate
        showLeaderboard("WIN RATE", "winrate");
        
        // Show detailed player stats
        showPlayerStats(players);
        
        System.out.println("\nSimulation completed successfully!");
    }
    
    private void showLeaderboard(String title, String sortBy) throws Exception {
        System.out.println("\nLEADERBOARD BY " + title);
        System.out.println("-" .repeat(40));
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/games/leaderboard/sorted?limit=10&sortBy=" + sortBy, 
            String.class
        );
        
        List<Player> leaderboard = objectMapper.readValue(response.getBody(), new TypeReference<List<Player>>() {});
        
        if (leaderboard.isEmpty()) {
            System.out.println("No players with games played yet.");
            return;
        }
        
        for (int i = 0; i < leaderboard.size(); i++) {
            Player player = leaderboard.get(i);
            System.out.printf("%d. %-10s | Wins: %d | Games: %d | Win Rate: %.1f%%\n",
                i + 1,
                player.getName(),
                player.getStats().getGamesWon(),
                player.getStats().getGamesPlayed(),
                player.getStats().getWinRate() * 100
            );
        }
    }
    
    private void showPlayerStats(List<Player> players) throws Exception {
        System.out.println("\nDETAILED PLAYER STATISTICS");
        System.out.println("-" .repeat(50));
        
        for (Player player : players) {
            ResponseEntity<Player> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/players/" + player.getId(),
                Player.class
            );
            
            Player updatedPlayer = response.getBody();
            if (updatedPlayer != null && updatedPlayer.getStats().getGamesPlayed() > 0) {
                System.out.printf(" %-10s | Played: %d | Won: %d | Lost: %d | Drawn: %d | Win Rate: %.1f%%\n",
                    updatedPlayer.getName(),
                    updatedPlayer.getStats().getGamesPlayed(),
                    updatedPlayer.getStats().getGamesWon(),
                    updatedPlayer.getStats().getGamesLost(),
                    updatedPlayer.getStats().getGamesDrawn(),
                    updatedPlayer.getStats().getWinRate() * 100
                );
            }
        }
    }
    
    private GameResult simulateGameSession(int gameNumber, List<Player> players) {
        try {
            // Create a new game
            ResponseEntity<String> gameResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/games",
                Map.of("name", "Concurrent Game " + gameNumber),
                String.class
            );
            
            String gameId = gameResponse.getBody().replaceAll("\"", "");
            
            // Select two random players
            List<Player> gamePlayers = players.stream()
                .sorted((a, b) -> Math.random() > 0.5 ? 1 : -1)
                .limit(2)
                .collect(Collectors.toList());
            
            Player player1 = gamePlayers.get(0);
            Player player2 = gamePlayers.get(1);
            
            // Add players to game
            restTemplate.postForEntity(
                "http://localhost:" + port + "/api/games/" + gameId + "/players",
                Map.of("playerId", player1.getId()),
                Void.class
            );
            
            restTemplate.postForEntity(
                "http://localhost:" + port + "/api/games/" + gameId + "/players",
                Map.of("playerId", player2.getId()),
                Void.class
            );
            
            // Simulate a quick game with random moves
            List<Integer> positions = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
            Collections.shuffle(positions);
            
            int moves = 0;
            String winner = null;
            String loser = null;
            
            // Make moves until game is complete (simplified - just make 3-5 moves)
            int totalMoves = 3 + (int)(Math.random() * 3); // 3-5 moves
            
            for (int i = 0; i < totalMoves && i < positions.size(); i++) {
                Player currentPlayer = (i % 2 == 0) ? player1 : player2;
                
                restTemplate.postForEntity(
                    "http://localhost:" + port + "/api/games/" + gameId + "/moves",
                    Map.of("playerId", currentPlayer.getId(), "position", positions.get(i)),
                    Void.class
                );
                
                moves++;
                
                // Check if game is complete
                ResponseEntity<String> statusResponse = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/games/" + gameId + "/status",
                    String.class
                );
                
                if (statusResponse.getBody().contains("COMPLETED")) {
                    winner = currentPlayer.getName();
                    loser = (currentPlayer == player1) ? player2.getName() : player1.getName();
                    break;
                }
            }
            
            // If no winner after moves, it's a draw
            if (winner == null) {
                winner = "Draw";
                loser = "Draw";
            }
            
            return new GameResult(gameNumber, winner, loser, moves);
            
        } catch (Exception e) {
            System.err.println("Error in game " + gameNumber + ": " + e.getMessage());
            return new GameResult(gameNumber, "Error", "Error", 0);
        }
    }
    
    private List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        
        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry"};
        
        for (int i = 0; i < count; i++) {
            String name = names[i % names.length] + (i >= names.length ? (i - names.length + 1) : "");
            String email = name.toLowerCase() + "_sim_" + System.currentTimeMillis() + "@example.com";
            
            ResponseEntity<Player> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/players",
                Map.of("name", name, "email", email),
                Player.class
            );
            
            players.add(response.getBody());
        }
        
        return players;
    }
    
    private void clearAllData() {
        restTemplate.delete("http://localhost:" + port + "/api/players/clear");
        restTemplate.delete("http://localhost:" + port + "/api/games/clear");
    }
    
    private static class GameResult {
        final int gameNumber;
        final String winner;
        final String loser;
        final int moves;
        
        GameResult(int gameNumber, String winner, String loser, int moves) {
            this.gameNumber = gameNumber;
            this.winner = winner;
            this.loser = loser;
            this.moves = moves;
        }
    }
}
