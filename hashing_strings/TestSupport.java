package hashing_strings;

import java.util.*;

/** Small dependency-free test harness. Checks stay enabled without java -ea. */
final class TestSupport {
    private static int groups;
    private static long checks;

    private TestSupport() {}

    static void run(String name, Runnable test) {
        try {
            test.run();
            groups++;
            System.out.println("  PASS " + name);
        } catch (Throwable failure) {
            throw new AssertionError("Failed: " + name, failure);
        }
    }

    static void check(boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("Condition was false");
    }

    static void eq(Object actual, Object expected) {
        checks++;
        boolean same = actual instanceof Number && expected instanceof Number
                ? ((Number) actual).longValue() == ((Number) expected).longValue()
                : Objects.deepEquals(actual, expected);
        if (!same) throw new AssertionError("Expected " + show(expected) + ", got " + show(actual));
    }

    static void rejects(Runnable operation) {
        checks++;
        try {
            operation.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    private static String show(Object value) {
        if (value instanceof int[]) return Arrays.toString((int[]) value);
        if (value instanceof char[]) return Arrays.toString((char[]) value);
        if (value instanceof Object[]) return Arrays.deepToString((Object[]) value);
        return String.valueOf(value);
    }

    static void summary() {
        System.out.println("\n" + groups + " test groups passed; " + checks + " checks.");
    }

    static int[] randomArray(Random random, int maxLength, int low, int high) {
        int[] a = new int[random.nextInt(maxLength + 1)];
        for (int i = 0; i < a.length; i++) a[i] = low + random.nextInt(high - low + 1);
        return a;
    }

    static String randomWord(Random random, String alphabet, int maxLength) {
        StringBuilder out = new StringBuilder();
        for (int n = random.nextInt(maxLength + 1); n > 0; n--)
            out.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return out.toString();
    }

    static String sorted(String s) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    static int count(String s, char target) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == target) count++;
        return count;
    }

    static int count(int[] a, int target) {
        int count = 0;
        for (int x : a) if (x == target) count++;
        return count;
    }

    static boolean palindrome(String s) {
        for (int l = 0, r = s.length() - 1; l < r; l++, r--)
            if (s.charAt(l) != s.charAt(r)) return false;
        return true;
    }

    static List<List<String>> canonicalGroups(List<List<String>> groups) {
        List<List<String>> result = new ArrayList<>();
        for (List<String> group : groups) {
            List<String> copy = new ArrayList<>(group);
            Collections.sort(copy);
            result.add(copy);
        }
        result.sort((a, b) -> {
            for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
                int comparison = a.get(i).compareTo(b.get(i));
                if (comparison != 0) return comparison;
            }
            return Integer.compare(a.size(), b.size());
        });
        return result;
    }
}
