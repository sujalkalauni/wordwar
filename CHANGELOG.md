# Changelog

All notable changes to WordWar will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- `getDictionarySize()` method in `WordScoringEngine` to expose dictionary size
- Expanded built-in word dictionary with 100+ common English words
- Scoring constants extracted to named fields for maintainability

### Changed
- `generateLetters()` now guarantees at least 3 vowels (was 2) for better playability
- Scoring magic numbers replaced with named constants (`BASE_SCORE_PER_LETTER`, `RARE_LETTER_BONUS`, etc.)

### Fixed
- Removed duplicate `"RACE"` entry from the word dictionary

---

## [1.0.0] - 2026-03-17

### Added
- Initial project setup with Spring Boot 3.2
- JWT-based authentication (register + login)
- Three game modes: `CLASSIC`, `SPEED`, `ANAGRAM`
- Custom `WordScoringEngine` with letter validation and scoring
- Game lifecycle: create, join, submit move, end round
- Global leaderboard endpoint
- Personal stats endpoint (`/api/games/stats/me`)
- SpringDoc OpenAPI / Swagger UI integration
- JUnit 5 + Mockito unit tests for engine and service layer
- H2 in-memory database for test profile
- MySQL 8 support for production profile
