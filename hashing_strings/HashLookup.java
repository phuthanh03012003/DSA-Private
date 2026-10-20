package hashing_strings;

import java.util.*;
import static hashing_strings.TestSupport.*;

/**
 * Hash maps and sets: complements, duplicates, first unique, and bijections.
 * Signs: unsorted pair sums, "seen before", nearby duplicates, or "same pattern".
 * Store the information a future element needs: an index, last index, or count.
 * Most methods take expected O(n) time and O(n) space. Sorted intersection
 * outputs additionally cost O(k log k) for k returned elements.
 * Gotchas: look up BEFORE inserting for Two Sum; count every earlier occurrence
 * for pair counting; use both directions to verify a one-to-one mapping.
 * Java: long arithmetic prevents overflow in complements and pair totals.
 * String indices and characters use Java UTF-16 units; see README conventions.
 * Adapted from hiepnnguyentcu/dsa-grand-slam; see ../ATTRIBUTION.md.
 */
public final class HashLookup {
    private HashLookup() {}

    /** LC 1. Return indices i < j, or an empty array if there is no pair. */
    public static int[] twoSum(int[] a, long target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int j = 0; j < a.length; j++) {
            long complement = target - a[j];
            if (complement >= Integer.MIN_VALUE && complement <= Integer.MAX_VALUE) {
                Integer i = seen.get((int) complement);
                if (i != null) return new int[]{i, j};
            }
            seen.put(a[j], j);
        }
        return new int[0];
    }

    /** Count index pairs i < j. The result can exceed Integer.MAX_VALUE. */
    public static long countPairs(int[] a, long target) {
        Map<Integer, Integer> seen = new HashMap<>();
        long total = 0;
        for (int x : a) {
            long complement = target - x;
            if (complement >= Integer.MIN_VALUE && complement <= Integer.MAX_VALUE)
                total += seen.getOrDefault((int) complement, 0);
            seen.merge(x, 1, Integer::sum);
        }
        return total;
    }

    /** LC 217. HashSet.add returns false when a value was already present. */
    public static boolean containsDuplicate(int[] a) {
        Set<Integer> seen = new HashSet<>();
        for (int x : a) if (!seen.add(x)) return true;
        return false;
    }

    /** LC 219. Equal values at distinct indices whose distance is at most k. */
    public static boolean containsNearbyDuplicate(int[] a, int k) {
        if (k <= 0) return false;
        Map<Integer, Integer> last = new HashMap<>();
        for (int i = 0; i < a.length; i++) {
            Integer previous = last.put(a[i], i);
            if (previous != null && i - previous <= k) return true;
        }
        return false;
    }

    /** LC 387. First unique character's index, or -1. Count, then scan in order. */
    public static int firstUniqueChar(String s) {
        Map<Character, Integer> counts = new HashMap<>();
        for (int i = 0; i < s.length(); i++) counts.merge(s.charAt(i), 1, Integer::sum);
        for (int i = 0; i < s.length(); i++) if (counts.get(s.charAt(i)) == 1) return i;
        return -1;
    }

    /** Value whose second occurrence comes earliest; null when none repeats. */
    public static Integer firstRepeating(int[] a) {
        Set<Integer> seen = new HashSet<>();
        for (int x : a) if (!seen.add(x)) return x;
        return null;
    }

    /** LC 205. A bijection must agree in both directions: ab -> aa is invalid. */
    public static boolean isIsomorphic(String s, String t) {
        if (s.length() != t.length()) return false;
        Map<Character, Character> forward = new HashMap<>(), backward = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i), b = t.charAt(i);
            Character oldB = forward.putIfAbsent(a, b), oldA = backward.putIfAbsent(b, a);
            if ((oldB != null && oldB != b) || (oldA != null && oldA != a)) return false;
        }
        return true;
    }

    /** LC 290. A one-to-one mapping between pattern characters and words. */
    public static boolean wordPattern(String pattern, String sentence) {
        String[] words = splitWords(sentence);
        if (pattern.length() != words.length) return false;
        Map<Character, String> forward = new HashMap<>();
        Map<String, Character> backward = new HashMap<>();
        for (int i = 0; i < words.length; i++) {
            char c = pattern.charAt(i);
            String oldWord = forward.putIfAbsent(c, words[i]);
            Character oldChar = backward.putIfAbsent(words[i], c);
            if ((oldWord != null && !oldWord.equals(words[i])) || (oldChar != null && oldChar != c))
                return false;
        }
        return true;
    }

    static String[] splitWords(String s) {
        String normalized = s.replaceAll("(?U)\\s+", " ").trim();
        return normalized.isEmpty() ? new String[0] : normalized.split(" ");
    }

    /** Assign a new ID at each first appearance: paper -> [0, 1, 0, 2, 3]. */
    public static <T> int[] firstOccurrenceSignature(List<T> sequence) {
        Map<T, Integer> first = new HashMap<>();
        int[] signature = new int[sequence.size()];
        int i = 0;
        for (T item : sequence) {
            first.putIfAbsent(item, first.size());
            signature[i++] = first.get(item);
        }
        return signature;
    }

    public static int[] firstOccurrenceSignature(String s) {
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) chars.add(s.charAt(i));
        return firstOccurrenceSignature(chars);
    }

    /** LC 349. Distinct common values, sorted. Does not mutate either input. */
    public static int[] intersection(int[] a, int[] b) {
        Set<Integer> available = new HashSet<>();
        for (int x : a) available.add(x);
        List<Integer> result = new ArrayList<>();
        for (int x : b) if (available.remove(x)) result.add(x);
        return result.stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    /** LC 350. Common values with multiplicity, sorted. */
    public static int[] intersectWithCounts(int[] a, int[] b) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int x : a) counts.merge(x, 1, Integer::sum);
        List<Integer> result = new ArrayList<>();
        for (int x : b) {
            int remaining = counts.getOrDefault(x, 0);
            if (remaining > 0) {
                result.add(x);
                counts.put(x, remaining - 1);
            }
        }
        return result.stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    static void runTests() {
        run("HashLookup.examples", () -> {
            eq(twoSum(new int[]{2, 7, 11, 15}, 9), new int[]{0, 1});
            eq(twoSum(new int[]{3, 3}, 6), new int[]{0, 1});
            eq(twoSum(new int[]{3, 2, 4}, 6), new int[]{1, 2});
            eq(firstUniqueChar("loveleetcode"), 2); eq(firstUniqueChar("aabb"), -1);
            check(isIsomorphic("egg", "add")); check(isIsomorphic("paper", "title"));
            check(!isIsomorphic("foo", "bar")); check(!isIsomorphic("ab", "aa"));
            check(!isIsomorphic("aa", "ab"));
            check(wordPattern("abba", "dog cat cat dog"));
            check(!wordPattern("abba", "dog dog dog dog"));
            check(!wordPattern("aaa", "dog dog"));
        });
        run("HashLookup.pairsVsBruteForce", () -> {
            Random r = new Random(1);
            for (int trial = 0; trial < 400; trial++) {
                int[] a = randomArray(r, 12, -5, 5); int target = r.nextInt(13) - 6;
                long expected = 0;
                for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++)
                    if ((long) a[i] + a[j] == target) expected++;
                int[] pair = twoSum(a, target);
                if (expected == 0) eq(pair, new int[0]);
                else check(pair.length == 2 && pair[0] < pair[1]
                        && (long) a[pair[0]] + a[pair[1]] == target);
                eq(countPairs(a, target), expected);
            }
        });
        run("HashLookup.duplicatesVsBruteForce", () -> {
            Random r = new Random(2);
            for (int trial = 0; trial < 400; trial++) {
                int[] a = randomArray(r, 12, 0, 9); int k = r.nextInt(6);
                boolean duplicate = false, nearby = false; Integer first = null;
                for (int j = 0; j < a.length; j++) for (int i = 0; i < j; i++) {
                    if (a[i] == a[j]) {
                        duplicate = true; nearby |= j - i <= k;
                        if (first == null) first = a[j];
                    }
                }
                eq(containsDuplicate(a), duplicate); eq(containsNearbyDuplicate(a, k), nearby);
                eq(firstRepeating(a), first);
            }
        });
        run("HashLookup.uniqueVsCountScan", () -> {
            Random r = new Random(3);
            for (int trial = 0; trial < 300; trial++) {
                String s = randomWord(r, "abcd", 10); int expected = -1;
                for (int i = 0; i < s.length(); i++) if (count(s, s.charAt(i)) == 1) { expected = i; break; }
                eq(firstUniqueChar(s), expected);
            }
        });
        run("HashLookup.isomorphicVsPairwiseRelations", () -> {
            Random r = new Random(4);
            for (int trial = 0; trial < 500; trial++) {
                String s = randomWord(r, "abc", 6), t = randomWord(r, "abc", 6);
                boolean expected = s.length() == t.length();
                if (expected) for (int i = 0; i < s.length(); i++) for (int j = 0; j < s.length(); j++)
                    expected &= (s.charAt(i) == s.charAt(j)) == (t.charAt(i) == t.charAt(j));
                eq(isIsomorphic(s, t), expected);
                eq(Arrays.equals(firstOccurrenceSignature(s), firstOccurrenceSignature(t)), expected);
            }
        });
        run("HashLookup.wordPatternVsSignature", () -> {
            Random r = new Random(5); String[] choices = {"dog", "cat", "fish"};
            for (int trial = 0; trial < 300; trial++) {
                String pattern = randomWord(r, "ab", 5); List<String> words = new ArrayList<>();
                for (int i = 0, n = r.nextInt(7); i < n; i++) words.add(choices[r.nextInt(3)]);
                boolean expected = Arrays.equals(firstOccurrenceSignature(pattern), firstOccurrenceSignature(words));
                eq(wordPattern(pattern, String.join(" ", words)), expected);
            }
        });
        run("HashLookup.intersectionsVsCountScan", () -> {
            Random r = new Random(6);
            for (int trial = 0; trial < 300; trial++) {
                int[] a = randomArray(r, 8, 0, 6), b = randomArray(r, 8, 0, 6);
                List<Integer> unique = new ArrayList<>(), multiple = new ArrayList<>();
                for (int x = 0; x <= 6; x++) {
                    int frequency = Math.min(count(a, x), count(b, x));
                    if (frequency > 0) unique.add(x);
                    for (int i = 0; i < frequency; i++) multiple.add(x);
                }
                eq(intersection(a, b), unique.stream().mapToInt(Integer::intValue).toArray());
                eq(intersectWithCounts(a, b), multiple.stream().mapToInt(Integer::intValue).toArray());
            }
        });
        run("HashLookup.javaBoundaries", () -> {
            eq(twoSum(new int[]{Integer.MAX_VALUE, 1}, Integer.MIN_VALUE), new int[0]);
            eq(twoSum(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}, 4294967294L), new int[]{0, 1});
            eq(countPairs(new int[100_000], 0), 4_999_950_000L);
            check(!containsNearbyDuplicate(new int[]{1, 1}, -1));
            check(wordPattern("ab", " \tdog\ncat\u00a0")); check(wordPattern("", " \n "));
            eq(firstOccurrenceSignature("paper"), new int[]{0, 1, 0, 2, 3});
        });
    }

    public static void main(String[] args) { runTests(); summary(); }
}
