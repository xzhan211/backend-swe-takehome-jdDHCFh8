package com.example.integration;

import com.example.model.Game;
import com.example.model.Player;
import com.example.model.PlayerStats;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.profiles.active=test"})
public class GameFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String player1Id;
    private String player2Id;
    private String gameId;

    @BeforeEach
    void setUp() throws Exception {
        baseUrl = "http://localhost:" + port;
        // Clear all existing data to ensure clean test state
        clearAllData();
        // Create fresh players and game
        String timestamp = String.valueOf(System.currentTimeMillis());
        player1Id = createPlayer("Alice", "alice" + timestamp + "@example.com");
        player2Id = createPlayer("Bob", "bob" + timestamp + "@example.com");
        gameId = createGame("Test Game");
    }

    @Test
    void testCompleteGameFlow() throws Exception {
        // Step 1: Verify initial game state
        verifyGameStatus(gameId, "WAITING");
        verifyGameMoves(gameId, 0);

        // Step 2: Add first player
        addPlayerToGame(gameId, player1Id);
        verifyGamePlayers(gameId, 1);

        // Step 3: Add second player
        addPlayerToGame(gameId, player2Id);
        verifyGamePlayers(gameId, 2);
        verifyGameStatus(gameId, "ACTIVE");

        // Step 4: Make moves (simulate tic-tac-toe)
        makeMove(gameId, player1Id, 0); // Top-left
        verifyGameMoves(gameId, 1);

        makeMove(gameId, player2Id, 1); // Top-center
        verifyGameMoves(gameId, 2);

        makeMove(gameId, player1Id, 3); // Middle-left
        verifyGameMoves(gameId, 3);

        makeMove(gameId, player2Id, 4); // Center
        verifyGameMoves(gameId, 4);

        makeMove(gameId, player1Id, 6); // Bottom-left (winning move - left column)
        verifyGameMoves(gameId, 5);

        // Step 5: Verify game completion
        verifyGameStatus(gameId, "COMPLETED");
        verifyGameWinner(gameId, player1Id);

        // Step 6: Verify player stats updated
        verifyPlayerStats(player1Id, 1, 1, 0, 0); // 1 game played, 1 won
        verifyPlayerStats(player2Id, 1, 0, 0, 1); // 1 game played, 1 lost

        // Step 7: Verify leaderboard
        verifyLeaderboard(player1Id, 1); // Alice should be #1
    }

    @Test
    void testMultipleGamesScenario() throws Exception {
        // Create multiple games and players
        String timestamp = String.valueOf(System.currentTimeMillis());
        String player3Id = createPlayer("Charlie", "charlie" + timestamp + "@example.com");
        String game2Id = createGame("Second Game");
        String game3Id = createGame("Third Game");

        // Game 1: Alice vs Bob - Alice wins with left column
        addPlayerToGame(gameId, player1Id);
        addPlayerToGame(gameId, player2Id);
        makeMove(gameId, player1Id, 0); // Top-left
        makeMove(gameId, player2Id, 1); // Top-center
        makeMove(gameId, player1Id, 3); // Middle-left
        makeMove(gameId, player2Id, 4); // Center
        makeMove(gameId, player1Id, 6); // Bottom-left (Alice wins)

        // Game 2: Bob vs Charlie - Bob wins with top row
        addPlayerToGame(game2Id, player2Id);
        addPlayerToGame(game2Id, player3Id);
        makeMove(game2Id, player2Id, 0); // Top-left
        makeMove(game2Id, player3Id, 3); // Middle-left
        makeMove(game2Id, player2Id, 1); // Top-center
        makeMove(game2Id, player3Id, 4); // Center
        makeMove(game2Id, player2Id, 2); // Top-right (Bob wins)

        // Game 3: Alice vs Charlie - Alice wins with diagonal
        addPlayerToGame(game3Id, player1Id);
        addPlayerToGame(game3Id, player3Id);
        makeMove(game3Id, player1Id, 0); // Top-left
        makeMove(game3Id, player3Id, 1); // Top-center
        makeMove(game3Id, player1Id, 4); // Center
        makeMove(game3Id, player3Id, 2); // Top-right
        makeMove(game3Id, player1Id, 8); // Bottom-right (Alice wins)

        // Verify final stats
        verifyPlayerStats(player1Id, 2, 2, 0, 0); // Alice: 2 games, 2 wins
        verifyPlayerStats(player2Id, 2, 1, 0, 1); // Bob: 2 games, 1 win, 1 loss
        verifyPlayerStats(player3Id, 2, 0, 0, 2); // Charlie: 2 games, 2 losses

        // Verify leaderboard order
        verifyLeaderboard(player1Id, 1); // Alice #1 (100% win rate)
        verifyLeaderboard(player2Id, 2); // Bob #2 (50% win rate)
        verifyLeaderboard(player3Id, 3); // Charlie #3 (0% win rate)

        // Verify game counts
        verifyGameCount(3);
        verifyActiveGames(0);
        verifyCompletedGames(3);
        verifyWaitingGames(0);
    }

    @Test
    void testPlayerSearchAndManagement() throws Exception {
        // Create additional players
        String timestamp = String.valueOf(System.currentTimeMillis());
        String player3Id = createPlayer("Alice Smith", "alice.smith" + timestamp + "@example.com");
        String player4Id = createPlayer("Alice Johnson", "alice.johnson" + timestamp + "@example.com");

        // Test player search
        verifyPlayerSearch("Alice", 3); // Should find all Alice players
        verifyPlayerSearch("Bob", 1);   // Should find only Bob
        verifyPlayerSearch("Charlie", 0); // Should find none

        // Test player update
        String updateTimestamp = String.valueOf(System.currentTimeMillis());
        updatePlayer(player1Id, "Alice Updated", "alice.updated" + updateTimestamp + "@example.com");
        verifyPlayer(player1Id, "Alice Updated", "alice.updated" + updateTimestamp + "@example.com");

        // Test player deletion
        deletePlayer(player4Id);
        verifyPlayerNotFound(player4Id);
        verifyPlayerSearch("Alice", 2); // Should now find only 2 Alice players
    }

    @Test
    void testGameStatusFiltering() throws Exception {
        // Create games with different statuses
        String waitingGameId = createGame("Waiting Game");
        String activeGameId = createGame("Active Game");
        String completedGameId = createGame("Completed Game");

        // Set up games
        addPlayerToGame(activeGameId, player1Id);
        addPlayerToGame(activeGameId, player2Id);

        addPlayerToGame(completedGameId, player1Id);
        addPlayerToGame(completedGameId, player2Id);
        makeMove(completedGameId, player1Id, 0); // Top-left
        makeMove(completedGameId, player2Id, 1); // Top-center
        makeMove(completedGameId, player1Id, 3); // Middle-left
        makeMove(completedGameId, player2Id, 4); // Center
        makeMove(completedGameId, player1Id, 6); // Bottom-left (wins with left column)

        // Verify status filtering
        verifyGamesByStatus("WAITING", 2); // Test Game (from setUp) + Waiting Game
        verifyGamesByStatus("ACTIVE", 1);
        verifyGamesByStatus("COMPLETED", 1);
    }

    // Helper methods for creating entities
    private String createPlayer(String name, String email) throws Exception {
        String requestBody = String.format("{\"name\":\"%s\",\"email\":\"%s\"}", name, email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Player> response = restTemplate.postForEntity(
                baseUrl + "/api/players", entity, Player.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        return response.getBody().getId();
    }

    private String createGame(String name) throws Exception {
        String requestBody = String.format("{\"name\":\"%s\"}", name);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Game> response = restTemplate.postForEntity(
                baseUrl + "/api/games", entity, Game.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        return response.getBody().getId();
    }

    private void addPlayerToGame(String gameId, String playerId) throws Exception {
        String requestBody = String.format("{\"playerId\":\"%s\"}", playerId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "/api/games/" + gameId + "/players", entity, Void.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
    }

    private void makeMove(String gameId, String playerId, int position) throws Exception {
        String requestBody = String.format("{\"playerId\":\"%s\",\"position\":%d}", playerId, position);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "/api/games/" + gameId + "/moves", entity, Void.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
    }

    private void updatePlayer(String playerId, String name, String email) throws Exception {
        String requestBody = String.format("{\"name\":\"%s\",\"email\":\"%s\"}", name, email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Player> response = restTemplate.exchange(
                baseUrl + "/api/players/" + playerId, HttpMethod.PUT, entity, Player.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
    }

    private void deletePlayer(String playerId) throws Exception {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/players/" + playerId, HttpMethod.DELETE, null, Void.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
    }

    // Helper methods for verification
    private void verifyGameStatus(String gameId, String expectedStatus) throws Exception {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/api/games/" + gameId + "/status", Map.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedStatus, response.getBody().get("status"), "Game status should match");
    }

    private void verifyGameMoves(String gameId, int expectedCount) throws Exception {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                baseUrl + "/api/games/" + gameId + "/moves", Object[].class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedCount, response.getBody().length, "Move count should match");
    }

    private void verifyGamePlayers(String gameId, int expectedCount) throws Exception {
        ResponseEntity<Game> response = restTemplate.getForEntity(
                baseUrl + "/api/games/" + gameId, Game.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedCount, response.getBody().getPlayers().size(), "Player count should match");
    }

    private void verifyGameWinner(String gameId, String expectedWinnerId) throws Exception {
        ResponseEntity<Player> response = restTemplate.getForEntity(
                baseUrl + "/api/games/" + gameId + "/winner", Player.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedWinnerId, response.getBody().getId(), "Winner ID should match");
    }

    private void verifyPlayerStats(String playerId, int gamesPlayed, int gamesWon, int gamesDrawn, int gamesLost) throws Exception {
        ResponseEntity<PlayerStats> response = restTemplate.getForEntity(
                baseUrl + "/api/players/" + playerId + "/stats", PlayerStats.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        PlayerStats stats = response.getBody();
        assertEquals(gamesPlayed, stats.getGamesPlayed(), "Games played should match");
        assertEquals(gamesWon, stats.getGamesWon(), "Games won should match");
        assertEquals(gamesDrawn, stats.getGamesDrawn(), "Games drawn should match");
        assertEquals(gamesLost, stats.getGamesLost(), "Games lost should match");
    }

    private void verifyLeaderboard(String playerId, int expectedPosition) throws Exception {
        ResponseEntity<Player[]> response = restTemplate.getForEntity(
                baseUrl + "/api/players/leaderboard", Player[].class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertTrue(expectedPosition <= response.getBody().length, "Position should be within leaderboard range");
        assertEquals(playerId, response.getBody()[expectedPosition - 1].getId(), "Player should be at expected position");
    }

    private void verifyPlayer(String playerId, String expectedName, String expectedEmail) throws Exception {
        ResponseEntity<Player> response = restTemplate.getForEntity(
                baseUrl + "/api/players/" + playerId, Player.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        Player player = response.getBody();
        assertEquals(expectedName, player.getName(), "Player name should match");
        assertEquals(expectedEmail, player.getEmail(), "Player email should match");
    }

    private void verifyPlayerNotFound(String playerId) throws Exception {
        ResponseEntity<Void> response = restTemplate.getForEntity(
                baseUrl + "/api/players/" + playerId, Void.class);
        
        assertTrue(response.getStatusCode().is4xxClientError(), "Should return client error for non-existent player");
    }

    private void verifyPlayerSearch(String searchTerm, int expectedCount) throws Exception {
        ResponseEntity<Player[]> response = restTemplate.getForEntity(
                baseUrl + "/api/players?name=" + searchTerm, Player[].class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedCount, response.getBody().length, "Move count should match");
    }

    private void verifyGameCount(int expectedCount) throws Exception {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/api/games/count", Map.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedCount, ((Number) response.getBody().get("count")).intValue(), "Game count should match");
    }

    private void verifyActiveGames(int expectedCount) throws Exception {
        ResponseEntity<Game[]> response = restTemplate.getForEntity(
                baseUrl + "/api/games/active", Game[].class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedCount, response.getBody().length, "Move count should match");
    }

    private void verifyCompletedGames(int expectedCount) throws Exception {
        ResponseEntity<Game[]> response = restTemplate.getForEntity(
                baseUrl + "/api/games/completed", Game[].class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedCount, response.getBody().length, "Move count should match");
    }

    private void verifyWaitingGames(int expectedCount) throws Exception {
        ResponseEntity<Game[]> response = restTemplate.getForEntity(
                baseUrl + "/api/games/waiting", Game[].class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedCount, response.getBody().length, "Move count should match");
    }

    private void verifyGamesByStatus(String status, int expectedCount) throws Exception {
        ResponseEntity<Game[]> response = restTemplate.getForEntity(
                baseUrl + "/api/games?status=" + status, Game[].class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Request should be successful");
        assertEquals(expectedCount, response.getBody().length, "Move count should match");
    }
    
    private void clearAllData() throws Exception {
        // Clear all games
        restTemplate.delete(baseUrl + "/api/games/clear");
        // Clear all players
        restTemplate.delete(baseUrl + "/api/players/clear");
    }
}