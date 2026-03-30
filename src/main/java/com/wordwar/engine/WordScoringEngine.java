package com.wordwar.engine;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Core word validation and scoring engine.
 *
 * Scoring rules:
 * - Base score: word length x 10
 * - Bonus for rare letters (Q, Z, X, J, K): +15 per rare letter
 * - Length bonus: 7+ letters = +50, 5-6 letters = +20
 * - Uses a built-in word list for validation (extendable to dictionary API)
 */
@Component
public class WordScoringEngine {

    // Scoring constants
    private static final int BASE_SCORE_PER_LETTER = 10;
    private static final int RARE_LETTER_BONUS = 15;
    private static final int LONG_WORD_BONUS = 50;   // 7+ letters
    private static final int MEDIUM_WORD_BONUS = 20; // 5-6 letters
    private static final int LONG_WORD_THRESHOLD = 7;
    private static final int MEDIUM_WORD_THRESHOLD = 5;

    private static final Set<Character> RARE_LETTERS = Set.of('Q', 'Z', 'X', 'J', 'K');

    // Built-in word list — extendable via dictionary API
    private static final Set<String> VALID_WORDS = new HashSet<>(Arrays.asList(
        "CAT", "CAR", "CARD", "CARE", "RACE", "CART", "ARC", "ACE", "ARE",
        "STAR", "RATS", "ARTS", "TARS", "TSAR", "TAR", "ART", "RAT", "SAT", "SIT",
        "STONE", "NOTES", "TONES", "SNORE", "STORE", "TENOR", "TONER", "STERN",
        "PLANES", "PLANET", "PLANER", "PANEL", "PLANE", "LEAN", "LANE", "PLAN",
        "PLATE", "PLEAT", "LEAPT", "PETAL", "TALES", "STALE", "STEAL", "LEAST",
        "WATER", "TRACE", "CRATE", "CATER", "REACT", "RACER",
        "WORDS", "SWORD", "RODS", "ROWS", "WORD", "LORD", "LORE",
        "GAME", "MAGE", "MEGA", "GALE", "MALE", "LAME", "MEAL", "TEAM",
        "PLAY", "CLAY", "CLAP", "FLAP", "CLAN",
        "BEST", "BETS", "BASTE", "BEATS", "BEAST", "ABETS",
        "RING", "GRIN", "RIND", "GRID", "GIRL", "GIRD",
        "ZONE", "ZEAL", "QUIZ", "QUIP", "JINX", "JINK", "AXLE", "EXAM",
        "NIGHT", "THING", "THONG", "TONG", "HONG",
        "SPRING", "SPRINT", "PRINTS", "GRINS", "RINGS", "PINGS", "KINGS",
        "TABLE", "BLEAT", "BLADE", "BALED", "DELTA", "DEALT",
        "CRANE", "NACRE", "RANCE", "CARVE", "RAVEN", "NAVEL", "VENAL",
        "BRIGHT", "BIGHT", "EIGHT", "TIGER", "GRIEF",
        "STRUM", "SLUMP", "PLUMS", "LUMPS", "SLUM", "SLIM", "LIMP",
        "FROST", "FORTS", "FORTE", "ROTS", "SORT",
        // Additional common words
        "ABLE", "ACID", "AGED", "ALSO", "AREA", "ARMY", "AWAY", "BABY",
        "BACK", "BAIL", "BAKE", "BALL", "BAND", "BANK", "BASE", "BATH",
        "BEAR", "BEAT", "BEEN", "BEER", "BELL", "BELT", "BIRD", "BITE",
        "BLUE", "BOLD", "BOLT", "BONE", "BOOK", "BORE", "BORN", "BULK",
        "BURN", "BUSH", "BUSY", "CAFE", "CAGE", "CAKE", "CALL", "CALM",
        "CAME", "CAMP", "CAPE", "CASE", "CASH", "CAST", "CAVE", "CELL",
        "CHAT", "CHEF", "CHIP", "CITE", "CITY", "CLUE", "COAL", "COAT",
        "CODE", "COIL", "COIN", "COLD", "COME", "COOK", "COOL", "COPE",
        "COPY", "CORE", "CORN", "COST", "COUP", "CREW", "CROP", "CURE",
        "CURL", "CUTE", "DARK", "DART", "DASH", "DATA", "DATE", "DAWN",
        "DAYS", "DEAD", "DEAL", "DEAR", "DECK", "DEED", "DEEP", "DEER",
        "DENY", "DESK", "DIET", "DIME", "DIRE", "DIRT", "DISK", "DOCK",
        "DOES", "DOME", "DONE", "DOOM", "DOOR", "DOSE", "DOTE", "DOVE",
        "DOWN", "DRAW", "DREW", "DROP", "DRUG", "DRUM", "DUAL", "DULL",
        "DUMP", "DUNE", "DUSK", "DUST", "DUTY", "EACH", "EARN", "EASE",
        "EAST", "EASY", "EDGE", "EPIC", "EVEN", "EVER", "EVIL", "FACE",
        "FACT", "FADE", "FAIL", "FAIR", "FAKE", "FALL", "FAME", "FARE",
        "FARM", "FAST", "FATE", "FAWN", "FEAR", "FEAT", "FEEL", "FEET",
        "FELL", "FELT", "FILE", "FILL", "FILM", "FIND", "FINE", "FIRE",
        "FIRM", "FISH", "FIST", "FIVE", "FLAG", "FLAT", "FLAW", "FLEW",
        "FLEX", "FLIP", "FLOW", "FOAM", "FOLD", "FOLK", "FOND", "FOOD",
        "FOOL", "FOOT", "FORD", "FORE", "FORK", "FORM", "FOUL", "FOUR",
        "FREE", "FROM", "FULL", "FUND", "FUSE", "FUSS", "GATE", "GAVE",
        "GAZE", "GEAR", "GERM", "GIFT", "GIVE", "GLAD", "GLOW", "GLUE",
        "GOAL", "GOES", "GOLD", "GOLF", "GONE", "GOOD", "GUST", "GUYS"
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
        int score = upper.length() * BASE_SCORE_PER_LETTER;
        // Rare letter bonus
        for (char c : upper.toCharArray()) {
            if (RARE_LETTERS.contains(c)) score += RARE_LETTER_BONUS;
        }
        // Length bonus
        if (upper.length() >= LONG_WORD_THRESHOLD) score += LONG_WORD_BONUS;
        else if (upper.length() >= MEDIUM_WORD_THRESHOLD) score += MEDIUM_WORD_BONUS;
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
        // Ensure at least 3 vowels for better playability
        String vowels = "AEIOU";
        sb.append(vowels.charAt(rand.nextInt(vowels.length())));
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

    /**
     * Returns the size of the current word dictionary.
     */
    public int getDictionarySize() {
        return VALID_WORDS.size();
    }
}
