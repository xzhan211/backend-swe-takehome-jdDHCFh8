package com.example.controller;

import com.example.model.Game;
import com.example.model.Player;
import com.example.service.GameService;
import com.example.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
public class GameControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GameService gameService;
    
    @MockBean
    private PlayerService playerService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Game testGame;
    private Player testPlayer;
    
    @BeforeEach
    void setUp() {
        testGame = new Game("Test Game");
        testPlayer = new Player("Test Player", "test@example.com");
    }
    
    @Test
    void createGame_ValidRequest_ReturnsCreatedGame() throws Exception {
        when(gameService.createGame(anyString())).thenReturn(testGame);
        
        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Game\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Game"));
    }
    
    @Test
    void getGame_ExistingGame_ReturnsGame() throws Exception {
        when(gameService.findById("game-id")).thenReturn(Optional.of(testGame));
        
        mockMvc.perform(get("/api/games/game-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Game"));
    }
    
    @Test
    void getGame_NonExistentGame_ReturnsNotFound() throws Exception {
        when(gameService.findById("non-existent")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/games/non-existent"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void addPlayerToGame_ValidRequest_ReturnsOk() throws Exception {
        when(playerService.findById("player-id")).thenReturn(Optional.of(testPlayer));
        when(gameService.addPlayerToGame(anyString(), any(Player.class))).thenReturn(true);
        
        mockMvc.perform(post("/api/games/game-id/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerId\":\"player-id\"}"))
                .andExpect(status().isOk());
    }
    
    @Test
    void makeMove_ValidRequest_ReturnsOk() throws Exception {
        when(gameService.makeMove(anyString(), anyString(), any(Integer.class))).thenReturn(true);
        
        mockMvc.perform(post("/api/games/game-id/moves")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerId\":\"player-id\",\"position\":4}"))
                .andExpect(status().isOk());
    }
    
    @Test
    void getGameStatus_ExistingGame_ReturnsStatus() throws Exception {
        when(gameService.getGameStatus("game-id")).thenReturn(Game.GameStatus.WAITING);
        
        mockMvc.perform(get("/api/games/game-id/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }
    
    @Test
    void getGameBoard_ExistingGame_ReturnsBoard() throws Exception {
        mockMvc.perform(get("/api/games/game-id/board"))
                .andExpect(status().isOk());
    }
    
    @Test
    void deleteGame_ExistingGame_ReturnsNoContent() throws Exception {
        when(gameService.deleteGame("game-id")).thenReturn(true);
        
        mockMvc.perform(delete("/api/games/game-id"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void deleteGame_NonExistentGame_ReturnsNotFound() throws Exception {
        when(gameService.deleteGame("non-existent")).thenReturn(false);
        
        mockMvc.perform(delete("/api/games/non-existent"))
                .andExpect(status().isNotFound());
    }
}

// TODO: Add API integration tests for core endpoints [ttt.todo.tests.api]