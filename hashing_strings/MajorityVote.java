package hashing_strings;

import java.util.*;
import static hashing_strings.TestSupport.*;

/**
 * Majority elements: a count map, then Boyer-Moore cancellation.
 * Signs: more than n/2 or n/3 occurrences, with constant extra space.
 * Different values cancel. A true majority survives, but ALWAYS verify the
 * candidate unless existence is guaranteed. For n/3, keep two candidates.
 * Compare existing candidates before filling an empty slot to avoid duplicates.
 * Voting takes O(n) time and O(1) space; counting uses O(n) space.
 * Adapted from hiepnnguyentcu/dsa-grand-slam; see ../ATTRIBUTION.md.
 */
public final class MajorityVote {
    private MajorityVote() {}

    /** Value occurring strictly more than n/2 times, or null. */
    public static Integer majorityCounter(int[] a) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int x : a) {
            int frequency = counts.merge(x, 1, Integer::sum);
            if (frequency > a.length / 2) return x;
        }
        return null;
    }

    /** LC 169 with a verification pass; returns null when no majority exists. */
    public static Integer majorityVote(int[] a) {
        int candidate = 0, balance = 0;
        for (int x : a) {
            if (balance == 0) candidate = x;
            balance += x == candidate ? 1 : -1;
        }
        int count = 0;
        for (int x : a) if (x == candidate) count++;
        return count > a.length / 2 ? Integer.valueOf(candidate) : null;
    }

    /** LC 229. All values occurring strictly more than n/3 times, sorted. */
    public static List<Integer> majorityII(int[] a) {
        int c1 = 0, c2 = 1, n1 = 0, n2 = 0;
        for (int x : a) {
            if (x == c1) n1++;
            else if (x == c2) n2++;
            else if (n1 == 0) { c1 = x; n1 = 1; }
            else if (n2 == 0) { c2 = x; n2 = 1; }
            else { n1--; n2--; }
        }
        n1 = 0; n2 = 0;
        for (int x : a) { if (x == c1) n1++; else if (x == c2) n2++; }
        List<Integer> out = new ArrayList<>();
        if (n1 > a.length / 3) out.add(c1);
        if (n2 > a.length / 3) out.add(c2);
        Collections.sort(out);
        return out;
    }

    static void runTests() {
        run("MajorityVote.examples", () -> {
            eq(majorityVote(new int[]{3, 2, 3}), 3);
            eq(majorityVote(new int[]{2, 2, 1, 1, 1, 2, 2}), 2);
            eq(majorityVote(new int[]{1, 1, 2, 2}), null); eq(majorityVote(new int[0]), null);
            eq(majorityII(new int[]{3, 2, 3}), List.of(3)); eq(majorityII(new int[]{1, 2}), List.of(1, 2));
            eq(majorityII(new int[]{1, 1, 1, 2, 2, 3, 3, 3}), List.of(1, 3));
        });
        run("MajorityVote.voteVsCountMap", () -> {
            Random r = new Random(31);
            for (int trial = 0; trial < 1000; trial++) {
                int[] a = randomArray(r, 12, 0, 3);
                if (a.length > 0 && r.nextBoolean()) {
                    int originalLength = a.length;
                    a = Arrays.copyOf(a, 2 * originalLength);
                    Arrays.fill(a, originalLength, a.length, a[0]);
                    for (int i = a.length - 1; i > 0; i--) { int j = r.nextInt(i + 1), x = a[i]; a[i] = a[j]; a[j] = x; }
                }
                eq(majorityVote(a), majorityCounter(a));
            }
        });
        run("MajorityVote.twoCandidatesVsCountScan", () -> {
            Random r = new Random(32);
            for (int trial = 0; trial < 1000; trial++) {
                int[] a = randomArray(r, 15, 0, 4); List<Integer> expected = new ArrayList<>();
                for (int x = 0; x <= 4; x++) if (count(a, x) > a.length / 3) expected.add(x);
                eq(majorityII(a), expected);
            }
        });
        run("MajorityVote.boundaries", () -> {
            eq(majorityCounter(new int[0]), null); eq(majorityII(new int[0]), List.of());
            eq(majorityVote(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE}), Integer.MIN_VALUE);
            eq(majorityII(new int[]{0, 0, 1, 1, 2}), List.of(0, 1));
            eq(majorityII(new int[]{-1, -1, -2, -2, 3}), List.of(-2, -1));
        });
    }

    public static void main(String[] args) { runTests(); summary(); }
}
