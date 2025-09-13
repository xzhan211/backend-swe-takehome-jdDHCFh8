# Backend SWE Take-Home Assignment - Java

## Overview

This is a **3-4 hour take-home assignment**. You will build a small, network-accessible backend web service that manages a turn-based, grid-driven game from pre-defined rules. Your assignment is tailored: a randomized (but reproducible) set of TODOs, features, and bugs has been embedded inline.

You should focus on:
- Clear, maintainable API handlers and service logic
- Robust input validation and error handling
- Simple, reliable tests (unit and integration)
- Helpful logs/metrics stubs where applicable

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080.

### Running Tests

```bash
mvn test
```

### Running the Simulation (DONE)

> Optional: You may create a simple simulation script or test that spins up your server, plays multiple sessions concurrently, and prints a small leaderboard summary.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           ├── Application.java
│   │           ├── controller/
│   │           ├── model/
│   │           ├── service/
│   │           └── repository/
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── example/
                ├── controller/
                ├── model/
                └── service/
```

## What You Need to Implement

### Selected Tasks (DONE)

#### TODOs
- Implement PlayerService (create/get/update/delete/search/stats)
- Complete players routes (update, delete, search)
- Add request logging middleware
- Add API integration tests for core endpoints
- Extend leaderboard endpoints (pagination, filters)
- Add Player and Game model input validation
- Validate Player model (name/email uniqueness, format)
- Complete games routes (status, join, moves, stats, delete, list)
- Add validation for game creation and move inputs

#### Feature Requests (DONE)
- Implement basic leaderboard endpoint
- Add basic rate limiting middleware

#### Bugs To Fix (DONE)

### Core Requirements (high-level) (DONE)

1. Turn-based rules on a finite grid with obvious invalid-move conditions
2. Multiple sessions can run concurrently; two players start a session
3. End a session on win or draw; expose session status
4. Leaderboard endpoint returning top users by wins or "efficiency" (lower moves per win is better)
5. A small simulation or test path that exercises the API

Additionally, look for inline TODOs in language-appropriate files. Examples:
- Java: `src/main/java/com/example/controller/*`, `src/main/java/com/example/service/*`, `src/main/java/com/example/model/*`, `src/main/java/com/example/Application.java`

> Focus on correctness, quality, and clarity. If you finish early, feel free to polish or extend.

## Notes

- Inline TODOs are your primary guide. GitHub Issues are intentionally disabled.
- Keep commits small and frequent with clear messages.
- You may add libraries if they help you implement tasks cleanly.

## Quick API Examples

Assuming your server is running on http://localhost:8080

### 1. Create Players
```bash
# Create player 1
curl -s -X POST http://localhost:8080/api/players -H 'Content-Type: application/json' -d '{"name":"Alice","email":"alice@example.com"}' | jq .

# Create player 2  
curl -s -X POST http://localhost:8080/api/players -H 'Content-Type: application/json' -d '{"name":"Bob","email":"bob@example.com"}' | jq .
```

### 2. Create a Game
```bash
curl -s -X POST http://localhost:8080/api/games -H 'Content-Type: application/json' -d '{"name":"Sample Game"}' | jq .
```

### 3. Add Players to Game
```bash
GAME_ID=<paste-game-id-from-create>
PLAYER1_ID=<paste-player1-id-from-create>
PLAYER2_ID=<paste-player2-id-from-create>

# Add player 1 to game
curl -s -X POST http://localhost:8080/api/games/$GAME_ID/players -H 'Content-Type: application/json' -d '{"playerId":"$PLAYER1_ID"}' | jq .

# Add player 2 to game
curl -s -X POST http://localhost:8080/api/games/$GAME_ID/players -H 'Content-Type: application/json' -d '{"playerId":"$PLAYER2_ID"}' | jq .
```

### 4. Make Moves and Check Status
```bash
# Make a move (position 0-8 for 3x3 grid)
curl -s -X POST http://localhost:8080/api/games/$GAME_ID/moves -H 'Content-Type: application/json' -d '{"playerId":"$PLAYER1_ID","position":0}' | jq .

# Check game status
curl -s http://localhost:8080/api/games/$GAME_ID/status | jq .

# Make another move
curl -s -X POST http://localhost:8080/api/games/$GAME_ID/moves -H 'Content-Type: application/json' -d '{"playerId":"$PLAYER2_ID","position":1}' | jq .

# Check game board
curl -s http://localhost:8080/api/games/$GAME_ID/board | jq .
```

### 5. Leaderboard Options
```bash
# Basic leaderboard (top 10 by win rate)
curl -s http://localhost:8080/api/games/leaderboard | jq .

# Leaderboard sorted by wins
curl -s http://localhost:8080/api/games/leaderboard/sorted?sortBy=wins&limit=5 | jq .

# Paginated leaderboard
curl -s http://localhost:8080/api/games/leaderboard/paginated?page=0&size=5 | jq .

# Paginated leaderboard sorted by wins
curl -s http://localhost:8080/api/games/leaderboard/paginated/sorted?page=0&size=5&sortBy=wins | jq .
```

### 6. Additional Useful Endpoints
```bash
# Get all games
curl -s http://localhost:8080/api/games | jq .

# Get active games only
curl -s http://localhost:8080/api/games/active | jq .

# Get player statistics
curl -s http://localhost:8080/api/players/$PLAYER1_ID/stats | jq .

# Get all players
curl -s http://localhost:8080/api/players | jq .

# Search players by name
curl -s http://localhost:8080/api/players?name=Alice | jq .
```

## Submission

1. Ensure tests pass (DONE)
2. Run the simulation script (DONE - integration test)
3. Update this README with any setup notes (DONE)
4. Submit your repository URL (DONE - https://github.com/xzhan211/backend-swe-takehome-jdDHCFh8)

Good luck! 🚀
