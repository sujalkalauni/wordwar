# WordWar ⚔️📝

![CI](https://github.com/sujalkalauni/wordwar/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)
![License](https://img.shields.io/badge/License-MIT-blue)

> Multiplayer word game backend — real-time battles, scoring engine, global leaderboard

A Spring Boot REST API for competitive word games. Create a game, invite opponents, and battle it out — who can find the highest-scoring word from a random set of letters?

---

## Game Modes

| Mode | How to Win |
|---|---|
| **CLASSIC** | Highest-scoring valid word from the 7 available letters wins each round |
| **SPEED** | First player to submit a valid word wins the round |
| **ANAGRAM** | Unscramble the exact target word — no partial credit |

---

## Scoring System

| Factor | Points |
|---|---|
| Base | Word length × 10 |
| 5–6 letter word | +20 bonus |
| 7+ letter word | +50 bonus |
| Rare letter (Q, Z, X, J, K) | +15 per letter |

Example: **QUIZ** = 4×10 + Q(+15) + Z(+15) = **70 points**

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2 |
| Auth | JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Word Engine | Custom scoring + validation engine |
| Docs | SpringDoc OpenAPI (Swagger) |
| Tests | JUnit 5, Mockito + H2 |

---

## Getting Started

### Prerequisites
- Java 17+
- MySQL 8
- Maven 3.8+

### Setup

```bash
git clone https://github.com/sujalkalauni/wordwar
cd wordwar
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Edit application.properties with your MySQL credentials
mvn spring-boot:run
```

API: `http://localhost:8083`
Swagger UI: `http://localhost:8083/swagger-ui.html`

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a player |
| POST | `/api/auth/login` | Login, get JWT |

### Games
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/games` | Create a game |
| GET | `/api/games/open` | List open games to join |
| POST | `/api/games/{id}/join` | Join a game |
| GET | `/api/games/{id}` | Get game state + scores |
| POST | `/api/games/{id}/moves` | Submit a word |
| GET | `/api/games/{id}/moves?round=1` | Get moves for a round |
| POST | `/api/games/{id}/round/end` | End current round |
| GET | `/api/games/leaderboard` | Global leaderboard |
| GET | `/api/games/stats/me` | Your personal stats |

---

## Example Playthrough

```bash
# 1. Register
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"lexmaster","email":"lex@words.com","password":"wordsmith"}'

# 2. Create a CLASSIC game
curl -X POST http://localhost:8083/api/games \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"mode":"CLASSIC","maxPlayers":2,"timeLimitSeconds":60,"totalRounds":3}'

# Response shows available letters e.g. "STARONE"

# 3. Submit a word
curl -X POST http://localhost:8083/api/games/1/moves \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"word":"STONE"}'
```

### Example move response
```json
{
  "word": "STONE",
  "valid": true,
  "pointsAwarded": 70,
  "round": 1,
  "playerUsername": "lexmaster",
  "feedback": "Valid word! +70 points"
}
```

### Global leaderboard
```json
[
  {"rank": 1, "username": "lexmaster", "totalPoints": 1240},
  {"rank": 2, "username": "wordninja", "totalPoints": 980}
]
```

---

## Running Tests

```bash
mvn test
```

Tests use H2 in-memory — no MySQL needed.

---

## Project Structure

```
src/
├── main/java/com/wordwar/
│   ├── engine/        # WordScoringEngine — validation, scoring, letter generation
│   ├── config/        # SecurityConfig, GlobalExceptionHandler
│   ├── controller/    # AuthController, GameController
│   ├── dto/           # All request/response DTOs
│   ├── entity/        # User, Game, GamePlayer, Move
│   ├── repository/    # JPA repos with leaderboard queries
│   ├── security/      # JwtUtils, JwtAuthFilter
│   └── service/       # AuthService, GameService
└── test/
    ├── engine/        # WordScoringEngineTest — scoring + validation logic
    └── service/       # GameServiceTest — game flow
```

---

## Author

**Sujal Kalauni** — [github.com/sujalkalauni](https://github.com/sujalkalauni)


---

## Roadmap

### v1.1.0 — Planned
- [ ] BLITZ mode: timed rounds with shrinking letter pool
- [ ] Websocket support for real-time multiplayer battles
- [ ] Docker + docker-compose setup for easy local dev
- [ ] Rate limiting on move submission endpoint
- [ ] Input validation with Bean Validation (`@NotBlank`, `@Size`)

### v1.2.0 — Future
- [ ] Integration with external dictionary API (e.g. Dictionary API)
- [ ] ELO-based player ranking system
- [ ] Game spectator mode (read-only game view)
- [ ] Admin dashboard for word dictionary management
- [ ] Redis caching for leaderboard queries

### v2.0.0 — Vision
- [ ] Frontend client (React + TypeScript)
- [ ] Mobile-friendly progressive web app
- [ ] AI opponent mode using word frequency analysis
- [ ] Tournament bracket system
