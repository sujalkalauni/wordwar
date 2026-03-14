package com.wordwar.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordScoringEngineTest {

    private WordScoringEngine engine;

    @BeforeEach
    void setUp() { engine = new WordScoringEngine(); }

    @Test
    void isValidWord_returnsTrueForKnownWordWithCorrectLetters() {
        // STAR can be formed from STARREX
        assertThat(engine.isValidWord("STAR", "STARREX")).isTrue();
    }

    @Test
    void isValidWord_returnsFalseWhenLetterNotAvailable() {
        // STAR cannot be formed from ABCDEFG (no S, T, A, R together)
        assertThat(engine.isValidWord("STAR", "BCDEFGH")).isFalse();
    }

    @Test
    void isValidWord_caseInsensitive() {
        assertThat(engine.isValidWord("star", "STARREX")).isTrue();
    }

    @Test
    void isValidWord_returnsFalseForUnknownWord() {
        assertThat(engine.isValidWord("ZZZQ", "ZZZQQQQQ")).isFalse();
    }

    @Test
    void scoreWord_shortWord_baseScore() {
        // CAT = 3 letters × 10 = 30
        assertThat(engine.scoreWord("CAT")).isEqualTo(30);
    }

    @Test
    void scoreWord_fiveLetterWord_getsLengthBonus() {
        // CRANE = 5 letters × 10 = 50 + 20 bonus = 70
        assertThat(engine.scoreWord("CRANE")).isEqualTo(70);
    }

    @Test
    void scoreWord_sevenLetterWord_getsHighBonus() {
        // PLANETS = 7 letters × 10 = 70 + 50 bonus = 120
        assertThat(engine.scoreWord("PLANETS")).isEqualTo(120);
    }

    @Test
    void scoreWord_rareLetterBonus() {
        // QUIZ = 4 letters × 10 = 40, Q=+15, Z=+15 = 70
        assertThat(engine.scoreWord("QUIZ")).isEqualTo(70);
    }

    @Test
    void generateLetters_returnsCorrectCount() {
        String letters = engine.generateLetters(7);
        assertThat(letters).hasSize(7);
    }

    @Test
    void generateLetters_containsAtLeastTwoVowels() {
        for (int i = 0; i < 10; i++) {
            String letters = engine.generateLetters(7);
            long vowelCount = letters.chars()
                    .filter(c -> "AEIOU".indexOf(c) >= 0)
                    .count();
            assertThat(vowelCount).isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void isCorrectAnagram_matchesExact() {
        assertThat(engine.isCorrectAnagram("STONE", "STONE")).isTrue();
        assertThat(engine.isCorrectAnagram("stone", "STONE")).isTrue();
        assertThat(engine.isCorrectAnagram("TONES", "STONE")).isFalse();
    }
}
