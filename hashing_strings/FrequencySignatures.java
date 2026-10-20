package hashing_strings;

import java.util.*;
import static hashing_strings.TestSupport.*;

/**
 * Frequency counting and canonical keys: anagrams, ransom notes, and grouping.
 * Signs: same letters in another order, enough letters, or grouping equivalents.
 * A frequency map identifies a multiset. Group by sorted letters, a 26-count
 * signature, or offsets modulo 26. Java arrays use identity equality, so an
 * int[] must not be used directly as a HashMap key; use a value representation.
 * Counting is O(n); sorted grouping O(m L log L); count grouping O(m (L+26)).
 * Marking uses O(n) time and O(1) auxiliary space, excluding returned values.
 * In-place marking temporarily changes the input, then restores it.
 * Adapted from hiepnnguyentcu/dsa-grand-slam; see ../ATTRIBUTION.md.
 */
public final class FrequencySignatures {
    private FrequencySignatures() {}

    private static Map<Character, Integer> frequencies(String s) {
        Map<Character, Integer> result = new HashMap<>();
        for (int i = 0; i < s.length(); i++) result.merge(s.charAt(i), 1, Integer::sum);
        return result;
    }

    /** LC 242. Supports Java char values beyond lowercase a-z. */
    public static boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;
        Map<Character, Integer> balance = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            balance.merge(s.charAt(i), 1, Integer::sum);
            balance.merge(t.charAt(i), -1, Integer::sum);
        }
        for (int value : balance.values()) if (value != 0) return false;
        return true;
    }

    /** LC 383. Magazine may contain extra letters; ransom may not. */
    public static boolean canConstruct(String ransom, String magazine) {
        Map<Character, Integer> have = frequencies(magazine);
        for (int i = 0; i < ransom.length(); i++) {
            char c = ransom.charAt(i); int remaining = have.getOrDefault(c, 0);
            if (remaining == 0) return false;
            have.put(c, remaining - 1);
        }
        return true;
    }

    /** LC 1347. Replacements in t needed to match s; lengths must be equal. */
    public static int minStepsToAnagram(String s, String t) {
        if (s.length() != t.length()) throw new IllegalArgumentException("Lengths must match");
        Map<Character, Integer> need = frequencies(s);
        for (int i = 0; i < t.length(); i++) need.merge(t.charAt(i), -1, Integer::sum);
        int missing = 0;
        for (int balance : need.values()) missing += Math.max(balance, 0);
        return missing;
    }

    /** LC 49. Sorted char sequence is a canonical anagram key. */
    public static List<List<String>> groupAnagramsSorted(List<String> words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String word : words) {
            char[] key = word.toCharArray(); Arrays.sort(key);
            groups.computeIfAbsent(new String(key), ignored -> new ArrayList<>()).add(word);
        }
        return new ArrayList<>(groups.values());
    }

    /** LC 49. 26 counts as a value key; lowercase a-z only. */
    public static List<List<String>> groupAnagramsCount(List<String> words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String word : words) {
            int[] counts = new int[26];
            for (int i = 0; i < word.length(); i++) counts[lowercaseIndex(word.charAt(i))]++;
            groups.computeIfAbsent(Arrays.toString(counts), ignored -> new ArrayList<>()).add(word);
        }
        return new ArrayList<>(groups.values());
    }

    private static int lowercaseIndex(char c) {
        if (c < 'a' || c > 'z') throw new IllegalArgumentException("Expected lowercase a-z");
        return c - 'a';
    }

    /** LC 249. Normalize offsets from the first letter; empty strings group together. */
    public static List<List<String>> groupShifted(List<String> words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String word : words) {
            StringBuilder key = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                int offset = (lowercaseIndex(word.charAt(i)) - lowercaseIndex(word.charAt(0)) + 26) % 26;
                key.append(offset).append(',');
            }
            groups.computeIfAbsent(key.toString(), ignored -> new ArrayList<>()).add(word);
        }
        return new ArrayList<>(groups.values());
    }

    private static void requireOneToN(int[] a) {
        for (int x : a) if (x < 1 || x > a.length)
            throw new IllegalArgumentException("Values must be in 1..n");
    }

    /** LC 442. Each value appears at most twice. Return second sightings in order. */
    public static List<Integer> findDuplicates(int[] a) {
        requireOneToN(a);
        List<Integer> out = new ArrayList<>();
        for (int x : a) {
            int value = Math.abs(x), slot = value - 1;
            if (a[slot] < 0) out.add(value);
            else a[slot] = -a[slot];
        }
        for (int i = 0; i < a.length; i++) a[i] = Math.abs(a[i]);
        return out;
    }

    /** LC 448. Return missing values in ascending order; restore input afterward. */
    public static List<Integer> findDisappeared(int[] a) {
        requireOneToN(a);
        for (int x : a) {
            int slot = Math.abs(x) - 1;
            a[slot] = -Math.abs(a[slot]);
        }
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0) out.add(i + 1);
            a[i] = Math.abs(a[i]);
        }
        return out;
    }

    static void runTests() {
        run("FrequencySignatures.examples", () -> {
            check(isAnagram("anagram", "nagaram")); check(!isAnagram("rat", "car"));
            check(canConstruct("aa", "aab")); check(!canConstruct("aa", "ab"));
            eq(minStepsToAnagram("leetcode", "practice"), 5);
            eq(canonicalGroups(groupAnagramsSorted(List.of("eat", "tea", "tan", "ate", "nat", "bat"))),
                    List.of(List.of("ate", "eat", "tea"), List.of("bat"), List.of("nat", "tan")));
            eq(canonicalGroups(groupShifted(List.of("abc", "bcd", "acef", "xyz", "az", "ba", "a", "z"))),
                    List.of(List.of("a", "z"), List.of("abc", "bcd", "xyz"), List.of("acef"), List.of("az", "ba")));
            eq(findDuplicates(new int[]{4, 3, 2, 7, 8, 2, 3, 1}), List.of(2, 3));
            eq(findDisappeared(new int[]{4, 3, 2, 7, 8, 2, 3, 1}), List.of(5, 6));
        });
        run("FrequencySignatures.anagramAndRansomVsSorting", () -> {
            Random r = new Random(11);
            for (int trial = 0; trial < 500; trial++) {
                String s = randomWord(r, "abc", 5), t = randomWord(r, "abc", 5);
                eq(isAnagram(s, t), sorted(s).equals(sorted(t)));
                boolean possible = true;
                for (char c : "abc".toCharArray()) possible &= count(s, c) <= count(t, c);
                eq(canConstruct(s, t), possible);
            }
        });
        run("FrequencySignatures.minStepsVsOverlap", () -> {
            Random r = new Random(12);
            for (int trial = 0; trial < 300; trial++) {
                int n = r.nextInt(8); StringBuilder s = new StringBuilder(), t = new StringBuilder();
                for (int i = 0; i < n; i++) { s.append((char) ('a' + r.nextInt(3))); t.append((char) ('a' + r.nextInt(3))); }
                int keep = 0;
                for (char c : "abc".toCharArray()) keep += Math.min(count(s.toString(), c), count(t.toString(), c));
                eq(minStepsToAnagram(s.toString(), t.toString()), n - keep);
            }
        });
        run("FrequencySignatures.groupsVsPairwiseOracle", () -> {
            Random r = new Random(13);
            for (int trial = 0; trial < 200; trial++) {
                List<String> words = new ArrayList<>();
                for (int i = 0, n = r.nextInt(11); i < n; i++) words.add(randomWord(r, "abcd", 4));
                List<List<String>> expected = new ArrayList<>();
                for (String w : words) {
                    List<String> matching = null;
                    for (List<String> group : expected) if (sorted(group.get(0)).equals(sorted(w))) { matching = group; break; }
                    if (matching == null) { matching = new ArrayList<>(); expected.add(matching); }
                    matching.add(w);
                }
                eq(canonicalGroups(groupAnagramsSorted(words)), canonicalGroups(expected));
                eq(canonicalGroups(groupAnagramsCount(words)), canonicalGroups(expected));
            }
        });
        run("FrequencySignatures.shiftedVsAll26Shifts", () -> {
            Random r = new Random(14);
            for (int trial = 0; trial < 200; trial++) {
                List<String> words = new ArrayList<>(); List<List<String>> expected = new ArrayList<>();
                for (int i = 0, n = r.nextInt(9); i < n; i++) words.add(randomWord(r, "abyz", 3));
                for (String w : words) {
                    List<String> matching = null;
                    for (List<String> group : expected) {
                        for (int k = 0; k < 26; k++) {
                            StringBuilder shifted = new StringBuilder();
                            for (char c : group.get(0).toCharArray()) shifted.append((char) ('a' + (c - 'a' + k) % 26));
                            if (shifted.toString().equals(w)) { matching = group; break; }
                        }
                        if (matching != null) break;
                    }
                    if (matching == null) { matching = new ArrayList<>(); expected.add(matching); }
                    matching.add(w);
                }
                eq(canonicalGroups(groupShifted(words)), canonicalGroups(expected));
            }
        });
        run("FrequencySignatures.markingAndRestoration", () -> {
            Random r = new Random(15);
            for (int trial = 0; trial < 300; trial++) {
                int n = 1 + r.nextInt(10); List<Integer> pool = new ArrayList<>();
                for (int x = 1; x <= n; x++) { pool.add(x); pool.add(x); }
                Collections.shuffle(pool, r);
                int[] a = pool.subList(0, n).stream().mapToInt(Integer::intValue).toArray(), before = a.clone();
                List<Integer> duplicates = new ArrayList<>(), missing = new ArrayList<>(); Set<Integer> seen = new HashSet<>();
                for (int x : a) if (!seen.add(x)) duplicates.add(x);
                for (int x = 1; x <= n; x++) if (count(a, x) == 0) missing.add(x);
                eq(findDuplicates(a), duplicates); eq(a, before);
                eq(findDisappeared(a), missing); eq(a, before);
            }
        });
        run("FrequencySignatures.boundaries", () -> {
            check(isAnagram("é中", "中é"));
            eq(groupAnagramsCount(List.of("", "")), List.of(List.of("", "")));
            eq(findDuplicates(new int[0]), List.of()); eq(findDisappeared(new int[0]), List.of());
            rejects(() -> groupAnagramsCount(List.of("A")));
            rejects(() -> minStepsToAnagram("a", ""));
            int[] invalid = {1, 0}; rejects(() -> findDisappeared(invalid)); eq(invalid, new int[]{1, 0});
        });
    }

    public static void main(String[] args) { runTests(); summary(); }
}
