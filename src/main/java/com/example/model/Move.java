package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
@Table(name = "moves")
public class Move {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Game ID is required")
    @Column(name = "game_id", nullable = false)
    private String gameId;
    
    @NotBlank(message = "Player ID is required")
    @Column(name = "player_id", nullable = false)
    private String playerId;
    
    @Min(value = 0, message = "Row position must be at least 0")
    @Max(value = 2, message = "Row position must be at most 2")
    @Column(name = "row_position", nullable = false)
    private int row;
    
    @Min(value = 0, message = "Column position must be at least 0")
    @Max(value = 2, message = "Column position must be at most 2")
    @Column(name = "col_position", nullable = false)
    private int col;
    
    @Min(value = 1, message = "Move number must be at least 1")
    @Max(value = 9, message = "Move number must be at most 9")
    @Column(name = "move_number", nullable = false)
    private int moveNumber;
    
    @Min(value = 0, message = "Position must be at least 0")
    @Max(value = 8, message = "Position must be at most 8")
    @Column(name = "position", nullable = false)
    private int position;
    
    @NotBlank(message = "Symbol is required")
    @Pattern(regexp = "^[XO]$", message = "Symbol must be either X or O")
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @NotNull(message = "Game reference is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", insertable = false, updatable = false)
    @JsonIgnore
    private Game game;
    
    @NotNull(message = "Player reference is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;
    
    @NotNull(message = "Created date is required")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Move() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Move(String gameId, String playerId, int row, int col, int moveNumber) {
        this();
        this.gameId = gameId;
        this.playerId = playerId;
        this.row = row;
        this.col = col;
        this.moveNumber = moveNumber;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public int getRow() {
        return row;
    }
    
    public void setRow(int row) {
        this.row = row;
    }
    
    public int getCol() {
        return col;
    }
    
    public void setCol(int col) {
        this.col = col;
    }
    
    public int getMoveNumber() {
        return moveNumber;
    }
    
    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    @Override
    public String toString() {
        return "Move{" +
                "id=" + id +
                ", gameId='" + gameId + '\'' +
                ", playerId='" + playerId + '\'' +
                ", row=" + row +
                ", col=" + col +
                ", moveNumber=" + moveNumber +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Move move = (Move) o;
        
        if (row != move.row) return false;
        if (col != move.col) return false;
        if (moveNumber != move.moveNumber) return false;
        if (id != null ? !id.equals(move.id) : move.id != null) return false;
        if (gameId != null ? !gameId.equals(move.gameId) : move.gameId != null) return false;
        return playerId != null ? playerId.equals(move.playerId) : move.playerId == null;
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (gameId != null ? gameId.hashCode() : 0);
        result = 31 * result + (playerId != null ? playerId.hashCode() : 0);
        result = 31 * result + row;
        result = 31 * result + col;
        result = 31 * result + moveNumber;
        return result;
    }
}
