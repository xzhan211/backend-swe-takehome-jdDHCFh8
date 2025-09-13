package com.example.integration;

import com.example.model.PaginatedResponse;
import com.example.model.Player;
import com.example.model.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LeaderboardPaginationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        baseUrl = "http://localhost:" + port;
        // Clear all data to ensure clean test state
        clearAllData();
        // Create test players with different win rates
        createTestPlayers();
    }

    @Test
    void testPaginationFirstPage() throws Exception {
        // Test first page with size 3
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players/leaderboard/paginated?page=0&size=3", 
                String.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String responseBody = response.getBody();
        
        assertNotNull(responseBody);
        
        // Parse the JSON response manually to verify structure
        PaginatedResponse<Player> result = objectMapper.readValue(responseBody, 
                objectMapper.getTypeFactory().constructParametricType(PaginatedResponse.class, Player.class));
        
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(0, result.getPage().getNumber());
        assertEquals(3, result.getPage().getSize());
        assertEquals(5, result.getPage().getTotalElements());
        assertEquals(2, result.getPage().getTotalPages());
        assertTrue(result.getPage().isFirst());
        assertFalse(result.getPage().isLast());
        
        // Verify players are sorted by win rate (highest first)
        // Just verify that we have 3 players and they're sorted by win rate
        assertTrue(result.getContent().get(0).getStats().getWinRate() >= 
                  result.getContent().get(1).getStats().getWinRate());
        assertTrue(result.getContent().get(1).getStats().getWinRate() >= 
                  result.getContent().get(2).getStats().getWinRate());
    }

    @Test
    void testPaginationSecondPage() throws Exception {
        // Test second page with size 3
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players/leaderboard/paginated?page=1&size=3",
                String.class);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        String responseBody = response.getBody();

        assertNotNull(responseBody);

        // Parse the JSON response manually to verify structure
        PaginatedResponse<Player> result = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(PaginatedResponse.class, Player.class));

        assertNotNull(result);
        assertEquals(2, result.getContent().size()); // Only 2 players left
        assertEquals(1, result.getPage().getNumber());
        assertEquals(3, result.getPage().getSize());
        assertEquals(5, result.getPage().getTotalElements());
        assertEquals(2, result.getPage().getTotalPages());
        assertFalse(result.getPage().isFirst());
        assertTrue(result.getPage().isLast());
        
        // Verify remaining players are sorted by win rate (highest first)
        assertTrue(result.getContent().get(0).getStats().getWinRate() >=
                  result.getContent().get(1).getStats().getWinRate());
    }

    @Test
    void testPaginationDefaultParameters() throws Exception {
        // Test with default parameters (page=0, size=10)
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players/leaderboard/paginated",
                String.class);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        String responseBody = response.getBody();

        assertNotNull(responseBody);

        // Parse the JSON response manually to verify structure
        PaginatedResponse<Player> result = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(PaginatedResponse.class, Player.class));

        assertNotNull(result);
        assertEquals(5, result.getContent().size()); // All 5 players
        assertEquals(0, result.getPage().getNumber());
        assertEquals(10, result.getPage().getSize());
        assertEquals(5, result.getPage().getTotalElements());
        assertEquals(1, result.getPage().getTotalPages());
        assertTrue(result.getPage().isFirst());
        assertTrue(result.getPage().isLast());
    }

    @Test
    void testPaginationEmptyResult() throws Exception {
        // Clear all players to test empty result
        restTemplate.delete(baseUrl + "/api/players/clear");
        
        ResponseEntity<PaginatedResponse> response = restTemplate.getForEntity(
                baseUrl + "/api/players/leaderboard/paginated?page=0&size=10", 
                PaginatedResponse.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
        PaginatedResponse<Player> result = response.getBody();
        
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getPage().getNumber());
        assertEquals(10, result.getPage().getSize());
        assertEquals(0, result.getPage().getTotalElements());
        assertEquals(0, result.getPage().getTotalPages());
        assertTrue(result.getPage().isFirst());
        assertTrue(result.getPage().isLast());
    }

    @Test
    void testPaginationInvalidPage() throws Exception {
        // Test invalid page number (page 10 when only 2 pages exist)
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players/leaderboard/paginated?page=10&size=3", 
                String.class);
        
        assertEquals(400, response.getStatusCode().value()); // Bad Request
    }

    @Test
    void testPaginationInvalidSize() throws Exception {
        // Test invalid page size (negative)
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players/leaderboard/paginated?page=0&size=-1", 
                String.class);
        
        assertEquals(400, response.getStatusCode().value()); // Bad Request
    }

    @Test
    void testPaginationNegativePage() throws Exception {
        // Test negative page number
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/players/leaderboard/paginated?page=-1&size=10", 
                String.class);
        
        assertEquals(400, response.getStatusCode().value()); // Bad Request
    }

    private void createTestPlayers() throws Exception {
        // Create players and simulate games to give them different win rates
        String aliceId = createPlayer("Alice", "alice@example.com"); // Will have 100% win rate
        String charlieId = createPlayer("Charlie", "charlie@example.com"); // Will have 50% win rate  
        String davidId = createPlayer("David", "david@example.com"); // Will have 33% win rate
        String eveId = createPlayer("Eve", "eve@example.com"); // Will have 25% win rate
        String bobId = createPlayer("Bob", "bob@example.com"); // Will have 0% win rate
        
        // Create games to simulate different win rates
        // Alice wins 4 games (100% win rate)
        for (int i = 0; i < 4; i++) {
            String gameId = createGame("Alice Game " + i);
            addPlayerToGame(gameId, aliceId);
            addPlayerToGame(gameId, bobId);
            makeMove(gameId, aliceId, 0); // Alice wins with left column
            makeMove(gameId, bobId, 1);
            makeMove(gameId, aliceId, 3);
            makeMove(gameId, bobId, 4);
            makeMove(gameId, aliceId, 6); // Alice wins
        }
        
        // Charlie wins 2, loses 2 (50% win rate)
        for (int i = 0; i < 2; i++) {
            String gameId = createGame("Charlie Win " + i);
            addPlayerToGame(gameId, charlieId);
            addPlayerToGame(gameId, bobId);
            makeMove(gameId, charlieId, 0);
            makeMove(gameId, bobId, 1);
            makeMove(gameId, charlieId, 3);
            makeMove(gameId, bobId, 4);
            makeMove(gameId, charlieId, 6); // Charlie wins
        }
        for (int i = 0; i < 2; i++) {
            String gameId = createGame("Charlie Loss " + i);
            addPlayerToGame(gameId, bobId);
            addPlayerToGame(gameId, charlieId);
            makeMove(gameId, bobId, 0);
            makeMove(gameId, charlieId, 1);
            makeMove(gameId, bobId, 3);
            makeMove(gameId, charlieId, 4);
            makeMove(gameId, bobId, 6); // Bob wins
        }
        
        // David wins 1, loses 2 (33% win rate)
        String gameId1 = createGame("David Win");
        addPlayerToGame(gameId1, davidId);
        addPlayerToGame(gameId1, bobId);
        makeMove(gameId1, davidId, 0);
        makeMove(gameId1, bobId, 1);
        makeMove(gameId1, davidId, 3);
        makeMove(gameId1, bobId, 4);
        makeMove(gameId1, davidId, 6); // David wins
        
        for (int i = 0; i < 2; i++) {
            String gameId = createGame("David Loss " + i);
            addPlayerToGame(gameId, bobId);
            addPlayerToGame(gameId, davidId);
            makeMove(gameId, bobId, 0);
            makeMove(gameId, davidId, 1);
            makeMove(gameId, bobId, 3);
            makeMove(gameId, davidId, 4);
            makeMove(gameId, bobId, 6); // Bob wins
        }
        
        // Eve wins 1, loses 3 (25% win rate)
        String gameId2 = createGame("Eve Win");
        addPlayerToGame(gameId2, eveId);
        addPlayerToGame(gameId2, bobId);
        makeMove(gameId2, eveId, 0);
        makeMove(gameId2, bobId, 1);
        makeMove(gameId2, eveId, 3);
        makeMove(gameId2, bobId, 4);
        makeMove(gameId2, eveId, 6); // Eve wins
        
        for (int i = 0; i < 3; i++) {
            String gameId = createGame("Eve Loss " + i);
            addPlayerToGame(gameId, bobId);
            addPlayerToGame(gameId, eveId);
            makeMove(gameId, bobId, 0);
            makeMove(gameId, eveId, 1);
            makeMove(gameId, bobId, 3);
            makeMove(gameId, eveId, 4);
            makeMove(gameId, bobId, 6); // Bob wins
        }
    }

    private String createPlayer(String name, String email) throws Exception {
        String requestBody = String.format("{\"name\":\"%s\",\"email\":\"%s\"}", name, email);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Player> response = restTemplate.postForEntity(
                baseUrl + "/api/players", entity, Player.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
        return response.getBody().getId();
    }
    
    private String createGame(String name) throws Exception {
        String requestBody = String.format("{\"name\":\"%s\"}", name);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Game> response = restTemplate.postForEntity(
                baseUrl + "/api/games", entity, Game.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
        return response.getBody().getId();
    }
    
    private void addPlayerToGame(String gameId, String playerId) throws Exception {
        String requestBody = String.format("{\"playerId\":\"%s\"}", playerId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "/api/games/" + gameId + "/players", entity, Void.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
    
    private void makeMove(String gameId, String playerId, int position) throws Exception {
        String requestBody = String.format("{\"playerId\":\"%s\",\"position\":%d}", playerId, position);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "/api/games/" + gameId + "/moves", entity, Void.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
    

    private void clearAllData() throws Exception {
        // Clear all games
        restTemplate.delete(baseUrl + "/api/games/clear");
        // Clear all players
        restTemplate.delete(baseUrl + "/api/players/clear");
    }
}
