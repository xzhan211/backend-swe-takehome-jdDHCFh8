package com.example.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Embeddable
public class PlayerStats {
    
    @Min(value = 0, message = "Games played cannot be negative")
    private int gamesPlayed = 0;
    
    @Min(value = 0, message = "Games won cannot be negative")
    private int gamesWon = 0;
    
    @Min(value = 0, message = "Games lost cannot be negative")
    private int gamesLost = 0;
    
    @Min(value = 0, message = "Games drawn cannot be negative")
    private int gamesDrawn = 0;
    
    @Min(value = 0, message = "Total moves cannot be negative")
    private int totalMoves = 0;
    
    @Min(value = 0, message = "Average moves per win cannot be negative")
    @Max(value = 9, message = "Average moves per win cannot exceed 9")
    private double averageMovesPerWin = 0.0;
    
    @DecimalMin(value = "0.0", message = "Win rate cannot be negative")
    @DecimalMax(value = "1.0", message = "Win rate cannot exceed 1.0")
    private double winRate = 0.0;
    
    @DecimalMin(value = "0.0", message = "Efficiency cannot be negative")
    @DecimalMax(value = "1.0", message = "Efficiency cannot exceed 1.0")
    private double efficiency = 0.0;
    
    // Default constructor
    public PlayerStats() {}
    
    // Constructor with initial values
    public PlayerStats(int gamesPlayed, int gamesWon, int gamesLost, int gamesDrawn, 
                      int totalMoves, double averageMovesPerWin, double winRate, double efficiency) {
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.gamesLost = gamesLost;
        this.gamesDrawn = gamesDrawn;
        this.totalMoves = totalMoves;
        this.averageMovesPerWin = averageMovesPerWin;
        this.winRate = winRate;
        this.efficiency = efficiency;
    }
    
    // Getters and Setters
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
    
    public int getGamesWon() {
        return gamesWon;
    }
    
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }
    
    public int getGamesLost() {
        return gamesLost;
    }
    
    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }
    
    public int getGamesDrawn() {
        return gamesDrawn;
    }
    
    public void setGamesDrawn(int gamesDrawn) {
        this.gamesDrawn = gamesDrawn;
    }
    
    public int getTotalMoves() {
        return totalMoves;
    }
    
    public void setTotalMoves(int totalMoves) {
        this.totalMoves = totalMoves;
    }
    
    public double getAverageMovesPerWin() {
        return averageMovesPerWin;
    }
    
    public void setAverageMovesPerWin(double averageMovesPerWin) {
        this.averageMovesPerWin = averageMovesPerWin;
    }
    
    public double getWinRate() {
        return winRate;
    }
    
    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }
    
    public double getEfficiency() {
        return efficiency;
    }
    
    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }
    
    // Helper methods to update stats
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
        updateWinRate();
    }
    
    public void incrementGamesWon() {
        this.gamesWon++;
        updateWinRate();
    }
    
    public void incrementGamesLost() {
        this.gamesLost++;
        updateWinRate();
    }
    
    public void incrementGamesDrawn() {
        this.gamesDrawn++;
        updateWinRate();
    }
    
    public void addMoves(int moves) {
        this.totalMoves += moves;
        updateAverageMovesPerWin();
    }
    
    private void updateWinRate() {
        if (gamesPlayed > 0) {
            this.winRate = (double) gamesWon / gamesPlayed;
        }
    }
    
    private void updateAverageMovesPerWin() {
        if (gamesWon > 0) {
            this.averageMovesPerWin = (double) totalMoves / gamesWon;
        }
    }
}
