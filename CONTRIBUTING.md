# Contributing to WordWar

Thank you for your interest in contributing to WordWar! Here's how you can help.

## Getting Started

1. **Fork** the repository
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/wordwar.git
   cd wordwar
   ```
3. **Create a branch** for your feature or fix:
   ```bash
   git checkout -b feat/your-feature-name
   ```

## Development Setup

- Java 17+
- Maven 3.8+
- MySQL 8 (or use H2 for tests)

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Fill in your MySQL credentials
mvn spring-boot:run
```

## Running Tests

```bash
mvn test
```

Tests use H2 in-memory — no MySQL required.

## Contribution Guidelines

### Code Style
- Follow standard Java naming conventions
- Use 4-space indentation
- Add Javadoc comments to public methods
- Keep methods short and focused (single responsibility)

### Commit Messages
Use [Conventional Commits](https://www.conventionalcommits.org/):
```
feat: add new game mode BLITZ
fix: correct scoring for 7-letter words
refactor: simplify letter generation logic
docs: update README with Docker setup
test: add unit tests for AnagramMode
```

### Pull Requests
- One feature/fix per PR
- Include tests for new functionality
- Update `CHANGELOG.md` under `[Unreleased]`
- Ensure all tests pass before submitting
- Reference any related issues: `Closes #42`

## What to Contribute

- New words for the dictionary
- New game modes
- Performance improvements to the scoring engine
- Better letter generation algorithms
- Documentation improvements
- Bug fixes

## Code of Conduct

Be respectful and constructive. This is a learning project — all skill levels welcome!

---

**Questions?** Open an issue or reach out via [github.com/sujalkalauni](https://github.com/sujalkalauni).
