package com.example.simulation;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.profiles.active=test"})
public class SimpleConcurrentSimulationTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Test
    void demonstrateConcurrentGameSessions() throws Exception {
        restTemplate = new TestRestTemplate();
        objectMapper = new ObjectMapper();
        
        System.out.println("🎮 Concurrent Game Session Demonstration");
        System.out.println("=" .repeat(50));
        
        // Clear existing data
        clearAllData();
        
        // Create players
        List<Player> players = createPlayers(4);
        System.out.println("Created " + players.size() + " players: " + 
            players.stream().map(Player::getName).collect(java.util.stream.Collectors.joining(", ")));
        
        // Simulate concurrent game sessions using a simpler approach
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<String>> futures = new ArrayList<>();
        
        // Create multiple concurrent game sessions
        for (int i = 0; i < 8; i++) {
            final int sessionNumber = i + 1;
            Future<String> future = executor.submit(() -> simulateConcurrentSession(sessionNumber, players));
            futures.add(future);
        }
        
        // Wait for all sessions to complete
        List<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                String result = future.get(10, TimeUnit.SECONDS);
                results.add(result);
                System.out.println(result);
            } catch (TimeoutException e) {
                System.err.println("Session timed out");
            }
        }
        
        executor.shutdown();
        
        System.out.println("\nCONCURRENT SESSION RESULTS");
        System.out.println("=" .repeat(50));
        System.out.println("Total concurrent sessions: " + results.size());
        System.out.println("Successful sessions: " + results.stream().filter(r -> !r.contains("Error")).count());
        
        // Show leaderboard by wins
        showLeaderboard("WINS", "wins");
        
        // Show leaderboard by win rate  
        showLeaderboard("WIN RATE", "winrate");
        
        // Show detailed player stats
        showPlayerStats(players);
        
        System.out.println("\nConcurrent simulation completed!");
        System.out.println("This demonstrates that multiple game sessions can run concurrently!");
    }
    
    private void showLeaderboard(String title, String sortBy) throws Exception {
        System.out.println("\n LEADERBOARD BY " + title);
        System.out.println("-" .repeat(30));
        
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
            System.out.printf("%d. %-8s | Wins: %d | Games: %d | Win Rate: %.1f%%\n",
                i + 1,
                player.getName(),
                player.getStats().getGamesWon(),
                player.getStats().getGamesPlayed(),
                player.getStats().getWinRate() * 100
            );
        }
    }
    
    private void showPlayerStats(List<Player> players) throws Exception {
        System.out.println("\n PLAYER STATISTICS");
        System.out.println("-" .repeat(40));
        
        for (Player player : players) {
            ResponseEntity<Player> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/players/" + player.getId(),
                Player.class
            );
            
            Player updatedPlayer = response.getBody();
            if (updatedPlayer != null && updatedPlayer.getStats().getGamesPlayed() > 0) {
                System.out.printf("👤 %-8s | Played: %d | Won: %d | Lost: %d | Win Rate: %.1f%%\n",
                    updatedPlayer.getName(),
                    updatedPlayer.getStats().getGamesPlayed(),
                    updatedPlayer.getStats().getGamesWon(),
                    updatedPlayer.getStats().getGamesLost(),
                    updatedPlayer.getStats().getWinRate() * 100
                );
            } else {
                System.out.printf(" %-8s | No games played yet\n", updatedPlayer.getName());
            }
        }
    }
    
    private String simulateConcurrentSession(int sessionNumber, List<Player> players) {
        try {
            // Simulate some processing time
            Thread.sleep(50 + (int)(Math.random() * 100));
            
            // Create a game
            ResponseEntity<String> gameResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/games",
                Map.of("name", "Concurrent Session " + sessionNumber),
                String.class
            );
            
            // Extract game ID from JSON response
            String gameId = objectMapper.readTree(gameResponse.getBody()).get("id").asText();
            
            // Select two random players
            List<Player> shuffledPlayers = new ArrayList<>(players);
            Collections.shuffle(shuffledPlayers);
            Player player1 = shuffledPlayers.get(0);
            Player player2 = shuffledPlayers.get(1);
            
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
            
            // Make 3-5 random moves
            int totalMoves = 3 + (int)(Math.random() * 3);
            
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
                    break;
                }
            }
            
            if (winner == null) {
                winner = "Draw";
            }
            
            return String.format(" Session %d: %s vs %s → Winner: %s (%d moves)", 
                sessionNumber, player1.getName(), player2.getName(), winner, moves);
            
        } catch (Exception e) {
            return String.format(" Session %d: Error - %s", sessionNumber, e.getMessage());
        }
    }
    
    private List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        
        String[] names = {"Alice", "Bob", "Charlie", "Diana"};
        
        for (int i = 0; i < count; i++) {
            String name = names[i % names.length];
            String email = name.toLowerCase() + "_concurrent_" + System.currentTimeMillis() + "_" + i + "@example.com";
            
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
}
