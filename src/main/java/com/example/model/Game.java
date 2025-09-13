package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank(message = "Game name is required")
    @Size(min = 1, max = 100, message = "Game name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_]+$", message = "Game name can only contain letters, numbers, spaces, hyphens, and underscores")
    @Column(nullable = false)
    private String name;
    
    @NotNull(message = "Game status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.WAITING;
    
    @NotNull(message = "Game board is required")
    @Size(min = 9, max = 9, message = "Game board must have exactly 9 cells")
    @ElementCollection
    @CollectionTable(name = "game_board", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "cell_value")
    private List<String> board = new ArrayList<>();
    
    @NotNull(message = "Players list is required")
    @Size(max = 2, message = "A game can have at most 2 players")
    @ManyToMany
    @JoinTable(
        name = "game_players",
        joinColumns = @JoinColumn(name = "game_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private List<Player> players = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "current_player_id")
    private Player currentPlayer;
    
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Player winner;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Move> moves = new ArrayList<>();
    
    @NotNull(message = "Created date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @NotNull(message = "Updated date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Game status enum
    public enum GameStatus {
        WAITING,    // Waiting for players to join
        ACTIVE,     // Game is in progress
        COMPLETED,  // Game has ended
        DRAW        // Game ended in a draw
    }
    
    // Default constructor
    public Game() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        initializeBoard();
    }
    
    // Constructor with name
    public Game(String name) {
        this();
        this.name = name;
    }
    
    // Initialize empty 3x3 board
    private void initializeBoard() {
        this.board = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            this.board.add(null);
        }
    }
    
    // Add player to game
    public boolean addPlayer(Player player) {
        if (players.size() >= 2) {
            return false; // Game is full
        }
        
        if (!players.contains(player)) {
            players.add(player);
            if (players.size() == 2) {
                status = GameStatus.ACTIVE;
                currentPlayer = players.get(0); // First player starts
            }
            return true;
        }
        return false;
    }
    
    // Make a move
    public boolean makeMove(Player player, int position) {
        if (status != GameStatus.ACTIVE) {
            return false; // Game not active
        }
        
        if (!player.equals(currentPlayer)) {
            return false; // Not player's turn
        }
        
        if (position < 0 || position >= 9 || board.get(position) != null) {
            return false; // Invalid position
        }
        
        // Make the move
        String symbol = players.indexOf(player) == 0 ? "X" : "O";
        board.set(position, symbol);
        
        // Create move record
        Move move = new Move();
        move.setGame(this);
        move.setPlayer(player);
        move.setPosition(position);
        move.setSymbol(symbol);
        moves.add(move);
        
        // Check for win or draw
        if (checkWin(symbol)) {
            status = GameStatus.COMPLETED;
            winner = player;
            // Update stats for completed game
            players.forEach(p -> p.getStats().incrementGamesPlayed());
            players.forEach(p -> p.getStats().addMoves(1));
            player.getStats().incrementGamesWon();
            players.stream()
                .filter(p -> !p.equals(player))
                .findFirst()
                .ifPresent(p -> p.getStats().incrementGamesLost());
        } else if (checkDraw()) {
            status = GameStatus.DRAW;
            // Update stats for drawn game
            players.forEach(p -> p.getStats().incrementGamesPlayed());
            players.forEach(p -> p.getStats().addMoves(1));
            players.forEach(p -> p.getStats().incrementGamesDrawn());
        } else {
            // Switch turns
            currentPlayer = players.get((players.indexOf(currentPlayer) + 1) % 2);
        }
        
        updatedAt = LocalDateTime.now();
        return true;
    }
    
    // Check if a player has won
    private boolean checkWin(String symbol) {
        // Check rows
        for (int i = 0; i < 9; i += 3) {
            if (board.get(i) != null && board.get(i).equals(symbol) &&
                board.get(i + 1) != null && board.get(i + 1).equals(symbol) &&
                board.get(i + 2) != null && board.get(i + 2).equals(symbol)) {
                return true;
            }
        }
        
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board.get(i) != null && board.get(i).equals(symbol) &&
                board.get(i + 3) != null && board.get(i + 3).equals(symbol) &&
                board.get(i + 6) != null && board.get(i + 6).equals(symbol)) {
                return true;
            }
        }
        
        // Check diagonals
        if (board.get(0) != null && board.get(0).equals(symbol) &&
            board.get(4) != null && board.get(4).equals(symbol) &&
            board.get(8) != null && board.get(8).equals(symbol)) {
            return true;
        }
        
        if (board.get(2) != null && board.get(2).equals(symbol) &&
            board.get(4) != null && board.get(4).equals(symbol) &&
            board.get(6) != null && board.get(6).equals(symbol)) {
            return true;
        }
        
        return false;
    }
    
    // Check if game is a draw
    private boolean checkDraw() {
        return board.stream().allMatch(cell -> cell != null);
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public GameStatus getStatus() {
        return status;
    }
    
    public void setStatus(GameStatus status) {
        this.status = status;
    }
    
    public List<String> getBoard() {
        return board;
    }
    
    public void setBoard(List<String> board) {
        this.board = board;
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
    
    public Player getWinner() {
        return winner;
    }
    
    public void setWinner(Player winner) {
        this.winner = winner;
    }
    
    public List<Move> getMoves() {
        return moves;
    }
    
    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
