package hashing_strings;

import java.util.*;
import static hashing_strings.TestSupport.*;

/**
 * Palindromic substrings: expand around a character OR a gap between characters.
 * Signs: contiguous palindrome, counting palindromes, O(1) auxiliary space.
 * Expansion is O(n^2) worst-case time, O(1) extra space (excluding output).
 * Manacher reuses mirrored radii for O(n) time and O(n) space.
 * Preserve the leftmost answer on a tie. String indices are Java UTF-16 units.
 * Adapted from hiepnnguyentcu/dsa-grand-slam; see ../ATTRIBUTION.md.
 */
public final class ExpandCentre {
    private ExpandCentre() {}

    private static int expandedLength(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) { left--; right++; }
        return right - left - 1; // bounds have advanced one step too far
    }

    /** LC 5. The leftmost longest palindromic substring. */
    public static String longestPalindrome(String s) {
        int bestStart = 0, bestLength = 0;
        for (int i = 0; i < s.length(); i++) {
            int length = Math.max(expandedLength(s, i, i), expandedLength(s, i, i + 1));
            if (length > bestLength) { bestStart = i - (length - 1) / 2; bestLength = length; }
        }
        return s.substring(bestStart, bestStart + bestLength);
    }

    /** LC 647. Count by position; use long because n*(n+1)/2 can exceed int. */
    public static long countPalindromes(String s) {
        long total = 0;
        for (int center = 0; center < s.length(); center++) {
            for (int gap = 0; gap <= 1; gap++) {
                int left = center, right = center + gap;
                while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
                    total++; left--; right++;
                }
            }
        }
        return total;
    }

    /** O(n) reference variant. Negative integer sentinels cannot collide with chars. */
    public static String longestPalindromeManacher(String s) {
        int[] transformed = new int[2 * s.length() + 3];
        transformed[0] = -3; transformed[transformed.length - 1] = -1;
        for (int i = 1; i < transformed.length - 1; i++)
            transformed[i] = (i % 2 == 1) ? -2 : s.charAt(i / 2 - 1);
        int[] radius = new int[transformed.length];
        int center = 0, right = 0, bestStart = 0, bestLength = 0;
        for (int i = 1; i < transformed.length - 1; i++) {
            if (i < right) radius[i] = Math.min(right - i, radius[2 * center - i]);
            while (transformed[i + radius[i] + 1] == transformed[i - radius[i] - 1]) radius[i]++;
            if (i + radius[i] > right) { center = i; right = i + radius[i]; }
            if (radius[i] > bestLength) { bestLength = radius[i]; bestStart = (i - radius[i]) / 2; }
        }
        return s.substring(bestStart, bestStart + bestLength);
    }

    private static String bruteLongest(String s) {
        String best = "";
        for (int i = 0; i < s.length(); i++) for (int j = i + 1; j <= s.length(); j++)
            if (j - i > best.length() && palindrome(s.substring(i, j))) best = s.substring(i, j);
        return best;
    }

    static void runTests() {
        run("ExpandCentre.examples", () -> {
            eq(longestPalindrome("babad"), "bab"); eq(longestPalindrome("cbbd"), "bb");
            eq(longestPalindrome(""), ""); eq(countPalindromes("abc"), 3); eq(countPalindromes("aaa"), 6);
        });
        run("ExpandCentre.longestVsBruteForceAndManacher", () -> {
            Random r = new Random(51);
            for (int trial = 0; trial < 500; trial++) {
                String s = randomWord(r, "ab", 14), expected = bruteLongest(s);
                eq(longestPalindrome(s), expected); eq(longestPalindromeManacher(s), expected);
            }
        });
        run("ExpandCentre.countVsBruteForce", () -> {
            Random r = new Random(52);
            for (int trial = 0; trial < 500; trial++) {
                String s = randomWord(r, "abc", 14); long expected = 0;
                for (int i = 0; i < s.length(); i++) for (int j = i + 1; j <= s.length(); j++)
                    if (palindrome(s.substring(i, j))) expected++;
                eq(countPalindromes(s), expected);
            }
        });
        run("ExpandCentre.allSameCharacter", () -> {
            String s = "a".repeat(300);
            eq(longestPalindrome(s), s); eq(countPalindromes(s), 300L * 301 / 2);
        });
        run("ExpandCentre.sentinelCharactersAndLargeManacher", () -> {
            Random r = new Random(53);
            for (int trial = 0; trial < 500; trial++) {
                String s = randomWord(r, "^#$\u0000a中", 14);
                eq(longestPalindromeManacher(s), bruteLongest(s));
            }
            eq(longestPalindromeManacher("a".repeat(100_000)), "a".repeat(100_000));
        });
    }

    public static void main(String[] args) { runTests(); summary(); }
}
