package com.example.integration;

import com.example.model.Game;
import com.example.model.Player;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiValidationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testPlayerValidationScenarios() throws Exception {
        // Test valid player creation
        ResponseEntity<Player> validResponse = createPlayer("Valid Player", "valid@example.com");
        assert validResponse.getStatusCode().is2xxSuccessful();
        assert "Valid Player".equals(validResponse.getBody().getName());
        assert "valid@example.com".equals(validResponse.getBody().getEmail());

        // Test invalid player creation - empty name
        ResponseEntity<String> emptyNameResponse = createPlayerWithError("", "test@example.com");
        assert emptyNameResponse.getStatusCode().is4xxClientError();

        // Test invalid player creation - null name
        ResponseEntity<String> nullNameResponse = createPlayerWithError(null, "test@example.com");
        assert nullNameResponse.getStatusCode().is4xxClientError();

        // Test invalid player creation - invalid email
        ResponseEntity<String> invalidEmailResponse = createPlayerWithError("Test Player", "invalid-email");
        assert invalidEmailResponse.getStatusCode().is4xxClientError();

        // Test invalid player creation - duplicate email
        ResponseEntity<String> duplicateEmailResponse = createPlayerWithError("Another Player", "valid@example.com");
        assert duplicateEmailResponse.getStatusCode().is4xxClientError();
    }

    @Test
    void testGameValidationScenarios() throws Exception {
        // Test valid game creation
        ResponseEntity<Game> validResponse = createGame("Valid Game");
        assert validResponse.getStatusCode().is2xxSuccessful();
        assert "Valid Game".equals(validResponse.getBody().getName());
        assert "WAITING".equals(validResponse.getBody().getStatus().name());

        // Test invalid game creation - empty name
        ResponseEntity<String> emptyNameResponse = createGameWithError("");
        assert emptyNameResponse.getStatusCode().is4xxClientError();

        // Test invalid game creation - null name
        ResponseEntity<String> nullNameResponse = createGameWithError(null);
        assert nullNameResponse.getStatusCode().is4xxClientError();

        // Test invalid game creation - name too long
        ResponseEntity<String> longNameResponse = createGameWithError("a".repeat(101));
        assert longNameResponse.getStatusCode().is4xxClientError();
    }

    @Test
    void testMoveValidationScenarios() throws Exception {
        // Create a game and players first
        String gameId = createGame("Test Game").getBody().getId();
        String playerId = createPlayer("Test Player", "test@example.com").getBody().getId();
        String player2Id = createPlayer("Test Player 2", "test2@example.com").getBody().getId();

        // Add players to game
        ResponseEntity<Void> addPlayerResponse = addPlayerToGame(gameId, playerId);
        assert addPlayerResponse.getStatusCode().is2xxSuccessful();
        
        ResponseEntity<Void> addPlayer2Response = addPlayerToGame(gameId, player2Id);
        assert addPlayer2Response.getStatusCode().is2xxSuccessful();

        // Test valid move
        ResponseEntity<Void> validMoveResponse = makeMove(gameId, playerId, 4);
        assert validMoveResponse.getStatusCode().is2xxSuccessful();

        // Test invalid move - position too high
        ResponseEntity<String> highPositionResponse = makeMoveWithError(gameId, playerId, 10);
        assert highPositionResponse.getStatusCode().is4xxClientError();

        // Test invalid move - negative position
        ResponseEntity<String> negativePositionResponse = makeMoveWithError(gameId, playerId, -1);
        assert negativePositionResponse.getStatusCode().is4xxClientError();

        // Test invalid move - invalid player ID format
        ResponseEntity<String> invalidPlayerIdResponse = makeMoveWithError(gameId, "invalid@player#id", 1);
        assert invalidPlayerIdResponse.getStatusCode().is4xxClientError();

        // Test invalid move - empty player ID
        ResponseEntity<String> emptyPlayerIdResponse = makeMoveWithError(gameId, "", 1);
        assert emptyPlayerIdResponse.getStatusCode().is4xxClientError();
    }

    @Test
    void testNotFoundScenarios() throws Exception {
        // Test non-existent player
        ResponseEntity<Void> playerNotFoundResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/players/non-existent-id", Void.class);
        assert playerNotFoundResponse.getStatusCode().is4xxClientError();

        // Test non-existent game
        ResponseEntity<Void> gameNotFoundResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games/non-existent-id", Void.class);
        assert gameNotFoundResponse.getStatusCode().is4xxClientError();

        // Test non-existent game moves
        ResponseEntity<Void> movesNotFoundResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games/non-existent-id/moves", Void.class);
        assert movesNotFoundResponse.getStatusCode().is4xxClientError();

        // Test non-existent game status
        ResponseEntity<Void> statusNotFoundResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games/non-existent-id/status", Void.class);
        assert statusNotFoundResponse.getStatusCode().is4xxClientError();

        // Test non-existent game board
        ResponseEntity<Void> boardNotFoundResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games/non-existent-id/board", Void.class);
        assert boardNotFoundResponse.getStatusCode().is4xxClientError();

        // Test non-existent game current player
        ResponseEntity<Void> currentPlayerNotFoundResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games/non-existent-id/current-player", Void.class);
        assert currentPlayerNotFoundResponse.getStatusCode().is4xxClientError();

        // Test non-existent game winner
        ResponseEntity<Void> winnerNotFoundResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games/non-existent-id/winner", Void.class);
        assert winnerNotFoundResponse.getStatusCode().is4xxClientError();

        // Test non-existent player stats
        ResponseEntity<Void> statsNotFoundResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/players/non-existent-id/stats", Void.class);
        assert statsNotFoundResponse.getStatusCode().is4xxClientError();
    }

    @Test
    void testInvalidGameStatusFilter() throws Exception {
        // Test invalid status filter
        ResponseEntity<String> invalidStatusResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games?status=INVALID_STATUS", String.class);
        assert invalidStatusResponse.getStatusCode().is4xxClientError();

        // Test valid status filters
        ResponseEntity<Game[]> waitingResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games?status=WAITING", Game[].class);
        assert waitingResponse.getStatusCode().is2xxSuccessful();

        ResponseEntity<Game[]> activeResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games?status=ACTIVE", Game[].class);
        assert activeResponse.getStatusCode().is2xxSuccessful();

        ResponseEntity<Game[]> completedResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games?status=COMPLETED", Game[].class);
        assert completedResponse.getStatusCode().is2xxSuccessful();

        ResponseEntity<Game[]> drawResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games?status=DRAW", Game[].class);
        assert drawResponse.getStatusCode().is2xxSuccessful();
    }

    @Test
    void testLeaderboardParameters() throws Exception {
        // Test default limit
        ResponseEntity<Player[]> defaultLimitResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/players/leaderboard", Player[].class);
        assert defaultLimitResponse.getStatusCode().is2xxSuccessful();

        // Test custom limit
        ResponseEntity<Player[]> customLimitResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/players/leaderboard?limit=5", Player[].class);
        assert customLimitResponse.getStatusCode().is2xxSuccessful();

        // Test games leaderboard
        ResponseEntity<Player[]> gamesLeaderboardResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games/leaderboard", Player[].class);
        assert gamesLeaderboardResponse.getStatusCode().is2xxSuccessful();

        ResponseEntity<Player[]> gamesLeaderboardLimitResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/games/leaderboard?limit=3", Player[].class);
        assert gamesLeaderboardLimitResponse.getStatusCode().is2xxSuccessful();
    }

    // Helper methods
    private ResponseEntity<Player> createPlayer(String name, String email) {
        String requestBody = String.format("{\"name\":\"%s\",\"email\":\"%s\"}", name, email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(getBaseUrl() + "/api/players", entity, Player.class);
    }

    private ResponseEntity<String> createPlayerWithError(String name, String email) {
        String requestBody = String.format("{\"name\":%s,\"email\":\"%s\"}", 
                name == null ? "null" : "\"" + name + "\"", email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(getBaseUrl() + "/api/players", entity, String.class);
    }

    private ResponseEntity<Game> createGame(String name) {
        String requestBody = String.format("{\"name\":\"%s\"}", name);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(getBaseUrl() + "/api/games", entity, Game.class);
    }

    private ResponseEntity<String> createGameWithError(String name) {
        String requestBody = String.format("{\"name\":%s}", 
                name == null ? "null" : "\"" + name + "\"");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(getBaseUrl() + "/api/games", entity, String.class);
    }

    private ResponseEntity<Void> addPlayerToGame(String gameId, String playerId) {
        String requestBody = String.format("{\"playerId\":\"%s\"}", playerId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(getBaseUrl() + "/api/games/" + gameId + "/players", entity, Void.class);
    }

    private ResponseEntity<Void> makeMove(String gameId, String playerId, int position) {
        String requestBody = String.format("{\"playerId\":\"%s\",\"position\":%d}", playerId, position);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(getBaseUrl() + "/api/games/" + gameId + "/moves", entity, Void.class);
    }

    private ResponseEntity<String> makeMoveWithError(String gameId, String playerId, int position) {
        String requestBody = String.format("{\"playerId\":\"%s\",\"position\":%d}", playerId, position);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(getBaseUrl() + "/api/games/" + gameId + "/moves", entity, String.class);
    }
}