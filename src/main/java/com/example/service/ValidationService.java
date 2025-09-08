package com.example.service;

import com.example.model.Game;
import com.example.model.Player;
import com.example.model.Move;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.util.regex.Pattern;

@Service
@Validated
public class ValidationService {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9\\s\\-_']{1,100}$"
    );
    
    // Player validation
    public void validatePlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        
        validatePlayerName(player.getName());
        validatePlayerEmail(player.getEmail());
    }
    
    public void validatePlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        
        if (name.length() > 100) {
            throw new IllegalArgumentException("Player name cannot exceed 100 characters");
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Player name contains invalid characters");
        }
    }
    
    public void validatePlayerEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Player email cannot be empty");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email cannot exceed 255 characters");
        }
    }
    
    // Game validation
    public void validateGame(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        if (game.getName() != null && game.getName().length() > 100) {
            throw new IllegalArgumentException("Game name cannot exceed 100 characters");
        }
        
        if (game.getPlayers() != null && game.getPlayers().size() > 2) {
            throw new IllegalArgumentException("Game cannot have more than 2 players");
        }
    }
    
    public void validateGameName(String name) {
        if (name != null && name.length() > 100) {
            throw new IllegalArgumentException("Game name cannot exceed 100 characters");
        }
        
        if (name != null && !NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Game name contains invalid characters");
        }
    }
    
    // Move validation
    public void validateMove(Move move) {
        if (move == null) {
            throw new IllegalArgumentException("Move cannot be null");
        }
        
        validateMovePosition(move.getPosition());
        validateMoveSymbol(move.getSymbol());
        
        if (move.getGame() == null) {
            throw new IllegalArgumentException("Move must be associated with a game");
        }
        
        if (move.getPlayer() == null) {
            throw new IllegalArgumentException("Move must be associated with a player");
        }
    }
    
    public void validateMovePosition(Integer position) {
        if (position == null) {
            throw new IllegalArgumentException("Move position cannot be null");
        }
        
        if (position < 0 || position > 8) {
            throw new IllegalArgumentException("Move position must be between 0 and 8");
        }
    }
    
    public void validateMoveSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Move symbol cannot be empty");
        }
        
        if (!symbol.equals("X") && !symbol.equals("O")) {
            throw new IllegalArgumentException("Move symbol must be either 'X' or 'O'");
        }
    }
    
    // Game state validation
    public void validateGameState(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        if (game.getStatus() == null) {
            throw new IllegalArgumentException("Game status cannot be null");
        }
        
        if (game.getBoard() == null || game.getBoard().size() != 9) {
            throw new IllegalArgumentException("Game board must have exactly 9 positions");
        }
        
        if (game.getPlayers() == null) {
            throw new IllegalArgumentException("Game players cannot be null");
        }
    }
    
    // Business logic validation
    public void validateGameJoin(Game game, Player player) {
        if (game == null || player == null) {
            throw new IllegalArgumentException("Game and player cannot be null");
        }
        
        if (game.getStatus() != Game.GameStatus.WAITING) {
            throw new IllegalArgumentException("Game is not accepting new players");
        }
        
        if (game.getPlayers().size() >= 2) {
            throw new IllegalArgumentException("Game is full");
        }
        
        if (game.getPlayers().contains(player)) {
            throw new IllegalArgumentException("Player is already in the game");
        }
    }
    
    public void validateGameMove(Game game, Player player, int position) {
        if (game == null || player == null) {
            throw new IllegalArgumentException("Game and player cannot be null");
        }
        
        if (game.getStatus() != Game.GameStatus.ACTIVE) {
            throw new IllegalArgumentException("Game is not active");
        }
        
        if (!game.getPlayers().contains(player)) {
            throw new IllegalArgumentException("Player is not in this game");
        }
        
        if (!player.equals(game.getCurrentPlayer())) {
            throw new IllegalArgumentException("Not player's turn");
        }
        
        if (position < 0 || position > 8) {
            throw new IllegalArgumentException("Invalid position");
        }
        
        if (game.getBoard().get(position) != null) {
            throw new IllegalArgumentException("Position already occupied");
        }
    }
    
    // Input sanitization
    public String sanitizePlayerName(String name) {
        if (name == null) {
            return null;
        }
        
        return name.trim()
            .replaceAll("[^a-zA-Z0-9\\s\\-_']", "")
            .replaceAll("\\s+", " ")
            .substring(0, Math.min(name.length(), 100));
    }
    
    public String sanitizeGameName(String name) {
        if (name == null) {
            return null;
        }
        
        return name.trim()
            .replaceAll("[^a-zA-Z0-9\\s\\-_']", "")
            .replaceAll("\\s+", " ")
            .substring(0, Math.min(name.length(), 100));
    }
    
    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        return email.trim().toLowerCase();
    }
}

// TODO: Add Player and Game model input validation [ttt.todo.model.validation]