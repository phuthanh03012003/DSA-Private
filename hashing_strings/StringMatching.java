package hashing_strings;

import java.util.*;
import static hashing_strings.TestSupport.*;

/**
 * Rolling hashes and KMP: substring search, repeated blocks, and palindromes.
 * Rabin-Karp rolls a fixed-length window: remove the outgoing contribution,
 * multiply by BASE, add the incoming character. Verify every hash match.
 * KMP's prefix table stores the longest proper prefix that is also a suffix.
 * On mismatch, fall back through this table instead of moving the text backward.
 * KMP: O(n+m) worst-case time, O(m) auxiliary space plus returned matches.
 * Rabin-Karp: O(n+m) expected time; collisions can worsen runtime, not correctness.
 * Longest duplicate: binary search length, O(n log n) expected time, O(n) space.
 * Java uses a smaller modulus than Python so intermediate products fit in long.
 * Adapted from hiepnnguyentcu/dsa-grand-slam; see ../ATTRIBUTION.md.
 */
public final class StringMatching {
    private static final long BASE = 131;
    private static final long MOD = 1_000_000_007L;

    private StringMatching() {}

    private static long power(int exponent) {
        long result = 1, base = BASE;
        while (exponent > 0) {
            if ((exponent & 1) != 0) result = result * base % MOD;
            base = base * base % MOD; exponent >>= 1;
        }
        return result;
    }

    private static long hashPrefix(String s, int length) {
        long hash = 0;
        for (int i = 0; i < length; i++) hash = (hash * BASE + s.charAt(i)) % MOD;
        return hash;
    }

    private static long roll(long hash, char outgoing, char incoming, long high) {
        hash = (hash - outgoing * high % MOD + MOD) % MOD;
        return (hash * BASE + incoming) % MOD;
    }

    /** LC 28. First UTF-16 index, -1 if absent, 0 for an empty pattern. */
    public static int rabinKarp(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        if (m == 0) return 0;
        if (m > n) return -1;
        long high = power(m - 1), wanted = hashPrefix(pattern, m), window = hashPrefix(text, m);
        for (int i = 0; i <= n - m; i++) {
            if (window == wanted && text.regionMatches(i, pattern, 0, m)) return i;
            if (i < n - m) window = roll(window, text.charAt(i), text.charAt(i + m), high);
        }
        return -1;
    }

    public static List<String> repeatedDna(String s) { return repeatedDna(s, 10); }

    /** LC 187. Sorted repeats: O(n*k + d*k*log d) time, O(n*k) space; d distinct repeats. */
    public static List<String> repeatedDna(String s, int k) {
        if (k <= 0) throw new IllegalArgumentException("k must be positive");
        Set<String> seen = new HashSet<>(); Set<String> duplicates = new TreeSet<>();
        for (int i = 0; i <= s.length() - k; i++) {
            String part = s.substring(i, i + k);
            if (!seen.add(part)) duplicates.add(part);
        }
        return new ArrayList<>(duplicates);
    }

    private static int duplicateOfLength(String s, int length) {
        long high = power(length - 1), hash = hashPrefix(s, length);
        Map<Long, List<Integer>> seen = new HashMap<>();
        seen.computeIfAbsent(hash, ignored -> new ArrayList<>()).add(0);
        for (int i = 1; i <= s.length() - length; i++) {
            hash = roll(hash, s.charAt(i - 1), s.charAt(i + length - 1), high);
            List<Integer> starts = seen.computeIfAbsent(hash, ignored -> new ArrayList<>());
            for (int previous : starts) if (s.regionMatches(previous, s, i, length)) return i;
            starts.add(i); // keep collision candidates rather than discarding them
        }
        return -1;
    }

    /** LC 1044. One longest duplicated substring, allowing overlapping occurrences. */
    public static String longestDupSubstring(String s) {
        int low = 1, high = s.length() - 1, bestStart = 0, bestLength = 0;
        while (low <= high) {
            int length = low + (high - low) / 2, start = duplicateOfLength(s, length);
            if (start >= 0) { bestStart = start; bestLength = length; low = length + 1; }
            else high = length - 1;
        }
        return s.substring(bestStart, bestStart + bestLength);
    }

    /** pi[i]: longest proper prefix of pattern[0..i] that is also its suffix. */
    public static int[] prefixFunction(String pattern) { return prefixFunction(pattern.chars().toArray()); }

    private static int[] prefixFunction(int[] pattern) {
        int[] pi = new int[pattern.length]; int matched = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (matched > 0 && pattern[i] != pattern[matched]) matched = pi[matched - 1];
            if (pattern[i] == pattern[matched]) matched++;
            pi[i] = matched;
        }
        return pi;
    }

    /** All matching starts, including overlaps; empty pattern matches every boundary. */
    public static List<Integer> kmpFindAll(String text, String pattern) {
        List<Integer> out = new ArrayList<>();
        if (pattern.isEmpty()) {
            for (int i = 0; i <= text.length(); i++) out.add(i);
            return out;
        }
        int[] pi = prefixFunction(pattern); int matched = 0;
        for (int i = 0; i < text.length(); i++) {
            while (matched > 0 && text.charAt(i) != pattern.charAt(matched)) matched = pi[matched - 1];
            if (text.charAt(i) == pattern.charAt(matched)) matched++;
            if (matched == pattern.length()) {
                out.add(i - matched + 1); matched = pi[matched - 1];
            }
        }
        return out;
    }

    /** LC 459. A non-empty block repeated at least twice; empty input returns false. */
    public static boolean repeatedSubstringPattern(String s) {
        if (s.isEmpty()) return false;
        int border = prefixFunction(s)[s.length() - 1];
        return border > 0 && s.length() % (s.length() - border) == 0;
    }

    /** LC 214. Add the fewest characters to the front to make a palindrome. */
    public static String shortestPalindrome(String s) {
        int n = s.length(); int[] joined = new int[2 * n + 1];
        joined[n] = -1; // outside the char range: safe even when s contains '#'
        for (int i = 0; i < n; i++) { joined[i] = s.charAt(i); joined[n + 1 + i] = s.charAt(n - 1 - i); }
        int prefixLength = prefixFunction(joined)[joined.length - 1];
        StringBuilder out = new StringBuilder();
        for (int i = n - 1; i >= prefixLength; i--) out.append(s.charAt(i));
        return out.append(s).toString();
    }

    static void runTests() {
        run("StringMatching.examples", () -> {
            eq(rabinKarp("sadbutsad", "sad"), 0); eq(rabinKarp("leetcode", "leeto"), -1);
            eq(repeatedDna("AAAAACCCCCAAAAACCCCCCAAAAAGGGTTT"), List.of("AAAAACCCCC", "CCCCCAAAAA"));
            eq(longestDupSubstring("banana"), "ana"); eq(longestDupSubstring("abcd"), "");
            eq(prefixFunction("aabaaab"), new int[]{0, 1, 0, 1, 2, 2, 3});
            eq(kmpFindAll("aaaa", "aa"), List.of(0, 1, 2));
            check(repeatedSubstringPattern("abab")); check(!repeatedSubstringPattern("aba"));
            eq(shortestPalindrome("aacecaaa"), "aaacecaaa"); eq(shortestPalindrome("abcd"), "dcbabcd");
        });
        run("StringMatching.searchVsLibrary", () -> {
            Random r = new Random(61);
            for (int trial = 0; trial < 1000; trial++) {
                String text = randomWord(r, "ab", 15), pattern = randomWord(r, "ab", 4);
                eq(rabinKarp(text, pattern), text.indexOf(pattern));
                List<Integer> expected = new ArrayList<>();
                for (int i = 0; i <= text.length() - pattern.length(); i++) if (text.startsWith(pattern, i)) expected.add(i);
                eq(kmpFindAll(text, pattern), expected);
            }
        });
        run("StringMatching.prefixVsBruteForce", () -> {
            Random r = new Random(62);
            for (int trial = 0; trial < 500; trial++) {
                String p = randomWord(r, "ab", 12); int[] expected = new int[p.length()];
                for (int i = 0; i < p.length(); i++) for (int k = 0; k <= i; k++)
                    if (p.substring(0, k).equals(p.substring(i + 1 - k, i + 1))) expected[i] = k;
                eq(prefixFunction(p), expected);
            }
        });
        run("StringMatching.duplicatesVsBruteForce", () -> {
            Random r = new Random(63);
            for (int trial = 0; trial < 300; trial++) {
                String s = randomWord(r, "ACGT", 14); int k = 1 + r.nextInt(4), bestLength = 0;
                Set<String> repeated = new TreeSet<>();
                for (int i = 0; i <= s.length() - k; i++) {
                    String candidate = s.substring(i, i + k);
                    if (s.indexOf(candidate, i + 1) >= 0) repeated.add(candidate);
                }
                eq(repeatedDna(s, k), new ArrayList<>(repeated));
                for (int length = 1; length < s.length(); length++) for (int i = 0; i <= s.length() - length; i++)
                    if (s.indexOf(s.substring(i, i + length), i + 1) >= 0) bestLength = Math.max(bestLength, length);
                String got = longestDupSubstring(s); eq(got.length(), bestLength);
                if (!got.isEmpty()) check(s.indexOf(got, s.indexOf(got) + 1) >= 0);
            }
        });
        run("StringMatching.repetitionAndPalindromeVsBruteForce", () -> {
            Random r = new Random(64);
            for (int trial = 0; trial < 500; trial++) {
                String s = randomWord(r, "ab#^$", 10); boolean repeated = false;
                for (int d = 1; d < s.length(); d++) if (s.length() % d == 0 && s.substring(0, d).repeat(s.length() / d).equals(s)) repeated = true;
                eq(repeatedSubstringPattern(s), repeated);
                int prefix = 0;
                for (int k = 0; k <= s.length(); k++) if (palindrome(s.substring(0, k))) prefix = k;
                String expected = new StringBuilder(s.substring(prefix)).reverse().append(s).toString();
                String got = shortestPalindrome(s); eq(got, expected); check(palindrome(got) && got.endsWith(s));
            }
        });
        run("StringMatching.collisionsAndBoundaries", () -> {
            // 97*131+131 == 98*131+0, but the two strings differ.
            eq(rabinKarp("a\u0083xxb\u0000", "b\u0000"), 4);
            eq(longestDupSubstring("a\u0083b\u0000"), "");
            eq(longestDupSubstring("a\u0083b\u0000b\u0000"), "b\u0000");
            eq(shortestPalindrome("#"), "#"); eq(shortestPalindrome(""), "");
            eq(kmpFindAll("", ""), List.of(0)); eq(prefixFunction(""), new int[0]);
            eq(repeatedDna("A", 2), List.of()); rejects(() -> repeatedDna("A", 0));
            String text = "a".repeat(50_000) + "b", pattern = "a".repeat(1000) + "b";
            eq(rabinKarp(text, pattern), 49_000); eq(kmpFindAll(text, pattern), List.of(49_000));
        });
    }

    public static void main(String[] args) { runTests(); summary(); }
}
