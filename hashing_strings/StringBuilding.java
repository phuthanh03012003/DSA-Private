package hashing_strings;

import java.util.*;
import java.util.regex.*;
import static hashing_strings.TestSupport.*;

/**
 * String construction: word reversal, compression, length prefixes, LCP, zigzag.
 * Use StringBuilder for repeated appends; String concatenation in a loop can
 * copy the growing prefix repeatedly. For char[] edits, use read/write pointers.
 * Length prefixes make delimiters inside data harmless: ["ab", ""] -> "2#ab0#".
 * Most operations are O(total length); LCP is O(number of strings * min length).
 * In-place reversal/compression use O(1) auxiliary space; builders store output.
 * Java strings and encoded lengths use UTF-16 units, not Python code-point counts.
 * Adapted from hiepnnguyentcu/dsa-grand-slam; see ../ATTRIBUTION.md.
 */
public final class StringBuilding {
    private StringBuilding() {}

    /** LC 151. Reverse words; collapse whitespace and remove padding. */
    public static String reverseWords(String s) {
        String[] words = HashLookup.splitWords(s);
        StringBuilder out = new StringBuilder();
        for (int i = words.length - 1; i >= 0; i--) {
            if (out.length() > 0) out.append(' ');
            out.append(words[i]);
        }
        return out.toString();
    }

    /** LC 186. Input has single spaces between words, with no padding. Mutates chars. */
    public static char[] reverseWordsInPlace(char[] chars) {
        reverse(chars, 0, chars.length - 1);
        int start = 0;
        for (int i = 0; i <= chars.length; i++) {
            if (i == chars.length || chars[i] == ' ') { reverse(chars, start, i - 1); start = i + 1; }
        }
        return chars;
    }

    private static void reverse(char[] chars, int left, int right) {
        while (left < right) { char c = chars[left]; chars[left++] = chars[right]; chars[right--] = c; }
    }

    /** LC 443. Return the valid prefix length after run-length encoding in place. */
    public static int compress(char[] chars) {
        int write = 0, read = 0;
        while (read < chars.length) {
            char c = chars[read]; int start = read;
            while (read < chars.length && chars[read] == c) read++;
            chars[write++] = c;
            int runLength = read - start;
            if (runLength > 1) for (char digit : Integer.toString(runLength).toCharArray()) chars[write++] = digit;
        }
        return write;
    }

    /** Include every count (even 1), but return s unless compression is shorter. */
    public static String compressString(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length();) {
            int end = i + 1;
            while (end < s.length() && s.charAt(end) == s.charAt(i)) end++;
            out.append(s.charAt(i)).append(end - i); i = end;
        }
        return out.length() < s.length() ? out.toString() : s;
    }

    /** LC 271. Each length counts Java UTF-16 units. Empty lists/strings are distinct. */
    public static String encode(List<String> strings) {
        StringBuilder out = new StringBuilder();
        for (String s : strings) out.append(s.length()).append('#').append(s);
        return out.toString();
    }

    /** Decode length-prefixed strings; reject malformed/truncated encodings. */
    public static List<String> decode(String encoded) {
        List<String> out = new ArrayList<>(); int i = 0;
        while (i < encoded.length()) {
            int delimiter = encoded.indexOf('#', i);
            if (delimiter <= i) throw new IllegalArgumentException("Missing length or delimiter");
            int length = 0;
            for (int p = i; p < delimiter; p++) {
                char c = encoded.charAt(p);
                if (c < '0' || c > '9') throw new IllegalArgumentException("Length must contain digits");
                int digit = c - '0';
                if (length > (Integer.MAX_VALUE - digit) / 10) throw new IllegalArgumentException("Length is too large");
                length = length * 10 + digit;
            }
            long end = (long) delimiter + 1 + length;
            if (end > encoded.length()) throw new IllegalArgumentException("Truncated payload");
            out.add(encoded.substring(delimiter + 1, (int) end)); i = (int) end;
        }
        return out;
    }

    /** LC 14. Compare one column across all strings, stopping at the first mismatch. */
    public static String longestCommonPrefix(List<String> strings) {
        if (strings.isEmpty()) return "";
        String first = strings.get(0);
        for (int i = 0; i < first.length(); i++) {
            for (int j = 1; j < strings.size(); j++) {
                String s = strings.get(j);
                if (i >= s.length() || s.charAt(i) != first.charAt(i)) return first.substring(0, i);
            }
        }
        return first;
    }

    /** LC 6. Walk rows down and up, then concatenate each row. numRows must be positive. */
    public static String zigzagConvert(String s, int numRows) {
        if (numRows <= 0) throw new IllegalArgumentException("numRows must be positive");
        if (numRows == 1 || numRows >= s.length()) return s;
        StringBuilder[] rows = new StringBuilder[numRows];
        for (int i = 0; i < numRows; i++) rows[i] = new StringBuilder();
        int row = 0, step = 1;
        for (int i = 0; i < s.length(); i++) {
            rows[row].append(s.charAt(i));
            if (row == 0) step = 1;
            else if (row == numRows - 1) step = -1;
            row += step;
        }
        StringBuilder result = new StringBuilder();
        for (StringBuilder line : rows) result.append(line);
        return result.toString();
    }

    private static String gridZigzag(String s, int rows) {
        if (rows == 1) return s;
        Map<Integer, Character> grid = new TreeMap<>(); int row = 0, col = 0; boolean down = true;
        for (char c : s.toCharArray()) {
            grid.put(row * (s.length() + 1) + col, c);
            if (down && row == rows - 1) down = false;
            else if (!down && row == 0) down = true;
            if (down) row++; else { row--; col++; }
        }
        StringBuilder out = new StringBuilder();
        for (char c : grid.values()) out.append(c);
        return out.toString();
    }

    static void runTests() {
        run("StringBuilding.examples", () -> {
            eq(reverseWords("  the sky   is blue "), "blue is sky the");
            eq(new String(reverseWordsInPlace("the sky is blue".toCharArray())), "blue is sky the");
            char[] chars = "abbbbbbbbbbbbc".toCharArray(); int n = compress(chars);
            eq(n, 5); eq(new String(chars, 0, n), "ab12c");
            eq(compressString("aabcccccaaa"), "a2b1c5a3"); eq(compressString("abc"), "abc");
            List<String> data = List.of("lint", "code", "", "#3#", "5#x"); eq(decode(encode(data)), data);
            eq(longestCommonPrefix(List.of("flower", "flow", "flight")), "fl");
            eq(longestCommonPrefix(List.of("dog", "racecar", "car")), "");
            eq(zigzagConvert("PAYPALISHIRING", 3), "PAHNAPLSIIGYIR");
            eq(zigzagConvert("PAYPALISHIRING", 4), "PINALSIGYAHRPI"); eq(zigzagConvert("AB", 1), "AB");
        });
        run("StringBuilding.wordReversalVsListReverse", () -> {
            Random r = new Random(41);
            for (int trial = 0; trial < 300; trial++) {
                List<String> words = new ArrayList<>();
                for (int i = 0, n = 1 + r.nextInt(5); i < n; i++) words.add("a" + randomWord(r, "ab", 2));
                String plain = String.join(" ", words); StringBuilder padded = new StringBuilder(" ".repeat(r.nextInt(3)));
                for (String word : words) padded.append(word).append(" ".repeat(1 + r.nextInt(3)));
                Collections.reverse(words); String expected = String.join(" ", words);
                eq(reverseWords(padded.toString()), expected); eq(new String(reverseWordsInPlace(plain.toCharArray())), expected);
            }
        });
        run("StringBuilding.compressionVsRegexRuns", () -> {
            Random r = new Random(42); Pattern runs = Pattern.compile("(.)\\1*", Pattern.DOTALL);
            for (int trial = 0; trial < 300; trial++) {
                StringBuilder input = new StringBuilder();
                for (int i = 0, n = r.nextInt(5); i < n; i++) input.append(r.nextBoolean() ? "a".repeat(1 + r.nextInt(12)) : "b".repeat(1 + r.nextInt(12)));
                String s = input.toString(); StringBuilder encoded = new StringBuilder(), full = new StringBuilder();
                Matcher matcher = runs.matcher(s);
                while (matcher.find()) {
                    int length = matcher.end() - matcher.start(); String c = matcher.group(1);
                    encoded.append(c); if (length > 1) encoded.append(length); full.append(c).append(length);
                }
                char[] chars = s.toCharArray(); int n = compress(chars);
                eq(new String(chars, 0, n), encoded.toString());
                eq(compressString(s), full.length() < s.length() ? full.toString() : s);
            }
        });
        run("StringBuilding.codecHostileRoundTrips", () -> {
            Random r = new Random(43);
            for (int trial = 0; trial < 500; trial++) {
                List<String> data = new ArrayList<>();
                for (int i = 0, n = r.nextInt(6); i < n; i++) data.add(randomWord(r, "a#1 \n中", 6));
                eq(decode(encode(data)), data);
            }
        });
        run("StringBuilding.prefixVsBruteForce", () -> {
            Random r = new Random(44);
            for (int trial = 0; trial < 500; trial++) {
                List<String> words = new ArrayList<>();
                for (int i = 0, n = 1 + r.nextInt(4); i < n; i++) words.add(randomWord(r, "ab", 4));
                String expected = "";
                for (int n = 0; n <= words.get(0).length(); n++) {
                    String candidate = words.get(0).substring(0, n); boolean valid = true;
                    for (String s : words) valid &= s.startsWith(candidate);
                    if (valid) expected = candidate;
                }
                eq(longestCommonPrefix(words), expected);
            }
        });
        run("StringBuilding.zigzagVsGrid", () -> {
            Random r = new Random(45);
            for (int trial = 0; trial < 300; trial++) {
                String s = randomWord(r, "abcdef", 20); int rows = 1 + r.nextInt(6);
                eq(zigzagConvert(s, rows), gridZigzag(s, rows));
            }
        });
        run("StringBuilding.boundariesAndInvalidEncoding", () -> {
            eq(reverseWords("\t\n"), ""); eq(compress(new char[0]), 0);
            eq(new String(reverseWordsInPlace(new char[0])), ""); eq(longestCommonPrefix(List.of()), "");
            eq(encode(List.of("ab", "")), "2#ab0#"); eq(decode(""), List.of());
            List<String> data = List.of("\uD83D\uDE00", "\u0000#", "é中", ""); eq(decode(encode(data)), data);
            for (String malformed : List.of("#", "2#a", "-1#a", "x#a", "999999999999#x", "1#a?")) rejects(() -> decode(malformed));
            rejects(() -> zigzagConvert("abc", 0));
        });
    }

    public static void main(String[] args) { runTests(); summary(); }
}
