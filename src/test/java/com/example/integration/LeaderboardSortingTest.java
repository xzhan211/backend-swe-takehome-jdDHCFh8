package com.example.integration;

import com.example.model.Player;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.profiles.active=test"})
public class LeaderboardSortingTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        restTemplate = new TestRestTemplate();
        objectMapper = new ObjectMapper();
        clearAllData();
    }

    private void clearAllData() {
        restTemplate.delete("http://localhost:" + port + "/api/players/clear");
        restTemplate.delete("http://localhost:" + port + "/api/games/clear");
    }

    @Test
    void testLeaderboardSortingByWins() throws Exception {
        // Create players
        Player player1 = createPlayer("Alice", "alice@example.com");
        Player player2 = createPlayer("Bob", "bob@example.com");
        Player player3 = createPlayer("Charlie", "charlie@example.com");

        // Test sorting by wins (should return empty list since no games played)
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/games/leaderboard/sorted?limit=10&sortBy=wins", 
            String.class
        );

        assertEquals(200, response.getStatusCode().value());
        
        List<Player> leaderboard = objectMapper.readValue(response.getBody(), new TypeReference<List<Player>>() {});
        
        // Should be empty since no players have played games
        assertEquals(0, leaderboard.size());
    }

    @Test
    void testLeaderboardSortingByWinRate() throws Exception {
        // Create players
        Player player1 = createPlayer("Alice", "alice@example.com");
        Player player2 = createPlayer("Bob", "bob@example.com");

        // Test sorting by win rate (should return empty list since no games played)
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/games/leaderboard/sorted?limit=10&sortBy=winrate", 
            String.class
        );

        assertEquals(200, response.getStatusCode().value());
        
        List<Player> leaderboard = objectMapper.readValue(response.getBody(), new TypeReference<List<Player>>() {});
        
        // Should be empty since no players have played games
        assertEquals(0, leaderboard.size());
    }

    @Test
    void testLeaderboardSortingInvalidParameter() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/games/leaderboard/sorted?limit=10&sortBy=invalid", 
            String.class
        );

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testLeaderboardPaginatedSorting() throws Exception {
        // Create players
        for (int i = 1; i <= 3; i++) {
            createPlayer("Player" + i, "player" + i + "@example.com");
        }

        // Test paginated sorting by wins
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/games/leaderboard/paginated/sorted?page=0&size=3&sortBy=wins", 
            String.class
        );

        assertEquals(200, response.getStatusCode().value());
        
        // Should return empty content since no games played
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("\"content\":[]"));
    }

    @Test
    void testLeaderboardDefaultSorting() throws Exception {
        // Create players
        createPlayer("Alice", "alice@example.com");
        createPlayer("Bob", "bob@example.com");

        // Test default sorting (winrate)
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/games/leaderboard/sorted?limit=10", 
            String.class
        );

        assertEquals(200, response.getStatusCode().value());
        
        List<Player> leaderboard = objectMapper.readValue(response.getBody(), new TypeReference<List<Player>>() {});
        
        // Should be empty since no players have played games
        assertEquals(0, leaderboard.size());
    }

    private Player createPlayer(String name, String email) {
        ResponseEntity<Player> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/players",
            Map.of("name", name, "email", email),
            Player.class
        );
        return response.getBody();
    }
}