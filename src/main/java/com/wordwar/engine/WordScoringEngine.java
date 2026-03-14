package com.wordwar.engine;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Core word validation and scoring engine.
 *
 * Scoring rules:
 * - Base score: word length × 10
 * - Bonus for rare letters (Q, Z, X, J): +15 per rare letter
 * - Length bonus: 7+ letters = +50, 5-6 letters = +20
 * - Uses a built-in word list for validation (extendable to dictionary API)
 */
@Component
public class WordScoringEngine {

    private static final Set<Character> RARE_LETTERS = Set.of('Q', 'Z', 'X', 'J', 'K');

    // Built-in word list — extendable via dictionary API
    private static final Set<String> VALID_WORDS = new HashSet<>(Arrays.asList(
        "CAT", "CAR", "CARD", "CARE", "RACE", "RACE", "CART", "ARC", "ACE", "ARE",
        "STAR", "RATS", "ARTS", "TARS", "TSAR", "TAR", "ART", "RAT", "SAT", "SIT",
        "STONE", "NOTES", "TONES", "SNORE", "STORE", "TENOR", "TONER", "STERN",
        "PLANES", "PLANET", "PLANER", "PANEL", "PLANE", "LEAN", "LANE", "PLAN",
        "PLATE", "PLEAT", "LEAPT", "PETAL", "TALES", "STALE", "STEAL", "LEAST",
        "WATER", "TRACE", "CRATE", "CATER", "REACT", "RACER",
        "WORDS", "SWORD", "RODS", "ROWS", "DORS", "WORD", "LORD", "LORE",
        "GAME", "MAGE", "MEGA", "GALE", "MALE", "LAME", "MEAL", "TEAM",
        "PLAY", "CLAY", "CLAP", "FLAP", "PLAN", "CLAN",
        "BEST", "BETS", "BASTE", "BEATS", "BEAST", "ABETS",
        "RING", "GRIN", "RIND", "DRING", "GRID", "GIRL", "GIRD",
        "ZONE", "ZEAL", "QUIZ", "QUIP", "JINX", "JINK", "AXLE", "EXAM",
        "NIGHT", "THING", "THONG", "TONG", "GOTH", "HONG", "HONING",
        "SPRING", "SPRINT", "PRINTS", "GRINS", "RINGS", "PINGS", "KINGS",
        "TABLE", "BLEAT", "BLADE", "BALED", "DELTA", "DEALT",
        "CRANE", "NACRE", "RANCE", "CARVE", "RAVEN", "NAVEL", "VENAL",
        "BRIGHT", "BIGHT", "EIGHT", "TIGER", "GRIEF", "FIBRE",
        "STRUM", "SLUMP", "PLUMS", "LUMPS", "SLUM", "SLIM", "LIMP",
        "FROST", "FORTS", "FORTE", "STORE", "ROTS", "SORT", "TORS"
    ));

    /**
     * Validate that a word can be formed from the available letters.
     * Each letter in the word must appear in availableLetters with correct frequency.
     */
    public boolean isValidWord(String word, String availableLetters) {
        if (word == null || word.isBlank()) return false;
        String upper = word.toUpperCase();

        // Check dictionary
        if (!VALID_WORDS.contains(upper)) return false;

        // Check letter availability
        Map<Character, Integer> letterPool = new HashMap<>();
        for (char c : availableLetters.toUpperCase().toCharArray()) {
            letterPool.merge(c, 1, Integer::sum);
        }

        for (char c : upper.toCharArray()) {
            int count = letterPool.getOrDefault(c, 0);
            if (count == 0) return false;
            letterPool.put(c, count - 1);
        }
        return true;
    }

    /**
     * Score a word based on length, rare letters, and bonus tiers.
     */
    public int scoreWord(String word) {
        String upper = word.toUpperCase();
        int score = upper.length() * 10;

        // Rare letter bonus
        for (char c : upper.toCharArray()) {
            if (RARE_LETTERS.contains(c)) score += 15;
        }

        // Length bonus
        if (upper.length() >= 7) score += 50;
        else if (upper.length() >= 5) score += 20;

        return score;
    }

    /**
     * Generate a random set of N letters biased toward playable combinations.
     */
    public String generateLetters(int count) {
        // Weighted letter distribution (vowels more common)
        String pool = "AAABBBCCCDDDEEEEEFFFGGGHHIIIJJKKLLLMMMNNNOOOPPPQRRRSSSTTTUUUVVWWXYYZ";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();

        // Ensure at least 2 vowels
        String vowels = "AEIOU";
        sb.append(vowels.charAt(rand.nextInt(vowels.length())));
        sb.append(vowels.charAt(rand.nextInt(vowels.length())));

        while (sb.length() < count) {
            sb.append(pool.charAt(rand.nextInt(pool.length())));
        }

        // Shuffle
        List<Character> chars = new ArrayList<>();
        for (char c : sb.toString().toCharArray()) chars.add(c);
        Collections.shuffle(chars);

        StringBuilder result = new StringBuilder();
        chars.forEach(result::append);
        return result.toString();
    }

    /**
     * For ANAGRAM mode — check if word exactly matches target (case-insensitive).
     */
    public boolean isCorrectAnagram(String word, String target) {
        return word.equalsIgnoreCase(target);
    }
}
