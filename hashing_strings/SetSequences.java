package hashing_strings;

import java.util.*;
import static hashing_strings.TestSupport.*;

/**
 * Set-based sequences: consecutive runs, missing positive, Sudoku, and cycles.
 * Start walking a run only when x-1 is absent. Iterate the set, not the array,
 * so duplicates do not repeat work. Expected O(n) time, O(n) space.
 * First missing positive places each value v in slot v-1: O(n) time, O(1)
 * extra space. Check the destination value before swapping to avoid a loop.
 * Java: guard x-1 and x+1 at int boundaries so runs do not wrap around.
 * Adapted from hiepnnguyentcu/dsa-grand-slam; see ../ATTRIBUTION.md.
 */
public final class SetSequences {
    private SetSequences() {}

    private static Set<Integer> values(int[] a) {
        Set<Integer> result = new HashSet<>();
        for (int x : a) result.add(x);
        return result;
    }

    /** LC 128. Length of the longest consecutive run; input order is irrelevant. */
    public static int longestConsecutive(int[] a) {
        Set<Integer> set = values(a); int best = 0;
        for (int start : set) {
            if (start != Integer.MIN_VALUE && set.contains(start - 1)) continue;
            int end = start, length = 1;
            while (end != Integer.MAX_VALUE && set.contains(end + 1)) { end++; length++; }
            best = Math.max(best, length);
        }
        return best;
    }

    /** All maximal [start, end] runs, sorted by start; O(n + r log r) time. */
    public static int[][] longestConsecutiveRuns(int[] a) {
        Set<Integer> set = values(a); List<int[]> result = new ArrayList<>();
        for (int start : set) {
            if (start != Integer.MIN_VALUE && set.contains(start - 1)) continue;
            int end = start;
            while (end != Integer.MAX_VALUE && set.contains(end + 1)) end++;
            result.add(new int[]{start, end});
        }
        result.sort(Comparator.comparingInt(pair -> pair[0]));
        return result.toArray(new int[0][]);
    }

    /** LC 41. Mutates a. The answer is always in 1..n+1. */
    public static int firstMissingPositive(int[] a) {
        for (int i = 0; i < a.length; i++) {
            while (a[i] >= 1 && a[i] <= a.length && a[a[i] - 1] != a[i]) {
                int destination = a[i] - 1, temp = a[destination];
                a[destination] = a[i]; a[i] = temp;
            }
        }
        for (int i = 0; i < a.length; i++) if (a[i] != i + 1) return i + 1;
        return a.length + 1;
    }

    /** LC 36. Exactly 9x9; '.' is empty. Invalid shape/symbols return false. */
    public static boolean isValidSudoku(char[][] board) {
        if (board.length != 9) return false;
        for (char[] row : board) if (row == null || row.length != 9) return false;
        Set<String> seen = new HashSet<>();
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            char value = board[r][c];
            if (value == '.') continue;
            if (value < '1' || value > '9') return false;
            if (!seen.add("row:" + r + ":" + value)
                    || !seen.add("col:" + c + ":" + value)
                    || !seen.add("box:" + r / 3 + ":" + c / 3 + ":" + value)) return false;
        }
        return true;
    }

    /** LC 202. Positive integers only; non-positive inputs return false. */
    public static boolean isHappy(int n) {
        if (n <= 0) return false;
        Set<Integer> seen = new HashSet<>();
        while (n != 1 && seen.add(n)) n = digitSquareSum(n);
        return n == 1;
    }

    private static int digitSquareSum(int n) {
        int total = 0;
        while (n > 0) { int digit = n % 10; total += digit * digit; n /= 10; }
        return total;
    }

    private static boolean bruteSudoku(char[][] b) {
        for (int unit = 0; unit < 27; unit++) {
            Set<Character> seen = new HashSet<>();
            for (int cell = 0; cell < 9; cell++) {
                int r, c;
                if (unit < 9) { r = unit; c = cell; }
                else if (unit < 18) { r = cell; c = unit - 9; }
                else { int box = unit - 18; r = box / 3 * 3 + cell / 3; c = box % 3 * 3 + cell % 3; }
                if (b[r][c] != '.' && !seen.add(b[r][c])) return false;
            }
        }
        return true;
    }

    static void runTests() {
        run("SetSequences.examples", () -> {
            eq(longestConsecutive(new int[]{100, 4, 200, 1, 3, 2}), 4);
            eq(longestConsecutive(new int[]{0, 3, 7, 2, 5, 8, 4, 6, 0, 1}), 9);
            eq(longestConsecutive(new int[0]), 0);
            eq(firstMissingPositive(new int[]{1, 2, 0}), 3);
            eq(firstMissingPositive(new int[]{3, 4, -1, 1}), 2);
            eq(firstMissingPositive(new int[]{7, 8, 9, 11, 12}), 1);
            eq(firstMissingPositive(new int[]{1, 1}), 2);
            check(isHappy(19)); check(!isHappy(2));
        });
        run("SetSequences.runsVsSorting", () -> {
            Random r = new Random(21);
            for (int trial = 0; trial < 500; trial++) {
                int[] a = randomArray(r, 15, -10, 10), ordered = Arrays.stream(a).distinct().sorted().toArray();
                List<int[]> expected = new ArrayList<>(); int best = 0;
                for (int i = 0; i < ordered.length;) {
                    int j = i;
                    while (j + 1 < ordered.length && (long) ordered[j] + 1 == ordered[j + 1]) j++;
                    expected.add(new int[]{ordered[i], ordered[j]}); best = Math.max(best, j - i + 1); i = j + 1;
                }
                eq(longestConsecutive(a), best);
                eq(longestConsecutiveRuns(a), expected.toArray(new int[0][]));
            }
        });
        run("SetSequences.missingVsBruteForce", () -> {
            Random r = new Random(22);
            for (int trial = 0; trial < 500; trial++) {
                int[] a = randomArray(r, 10, -3, 10); int expected = 1;
                while (count(a, expected) > 0) expected++;
                eq(firstMissingPositive(a.clone()), expected);
            }
        });
        run("SetSequences.sudokuVsIndependentUnits", () -> {
            Random random = new Random(23); char[][] solved = new char[9][9];
            for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) solved[r][c] = (char) ('1' + (r * 3 + r / 3 + c) % 9);
            check(isValidSudoku(solved));
            for (int trial = 0; trial < 300; trial++) {
                char[][] b = new char[9][9];
                for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) b[r][c] = random.nextDouble() < 0.4 ? solved[r][c] : '.';
                for (int i = 0, n = random.nextInt(3); i < n; i++) b[random.nextInt(9)][random.nextInt(9)] = (char) ('1' + random.nextInt(9));
                eq(isValidSudoku(b), bruteSudoku(b));
            }
        });
        run("SetSequences.happyVsFloyd", () -> {
            for (int n = 1; n < 1000; n++) {
                int slow = n, fast = digitSquareSum(n);
                while (fast != 1 && slow != fast) { slow = digitSquareSum(slow); fast = digitSquareSum(digitSquareSum(fast)); }
                eq(isHappy(n), fast == 1);
            }
        });
        run("SetSequences.integerBoundaries", () -> {
            int[] a = {Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1};
            eq(longestConsecutive(a), 2);
            eq(longestConsecutiveRuns(a), new int[][]{{Integer.MIN_VALUE, Integer.MIN_VALUE + 1}, {Integer.MAX_VALUE - 1, Integer.MAX_VALUE}});
            eq(firstMissingPositive(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE, 1}), 2);
            check(!isHappy(0)); check(!isHappy(-1)); check(!isValidSudoku(new char[8][9]));
        });
    }

    public static void main(String[] args) { runTests(); summary(); }
}
