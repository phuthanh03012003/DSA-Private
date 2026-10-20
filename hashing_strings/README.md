# Hashing & Strings — Java

Seven technique families, 45 algorithm variants, and 43 executable test groups. Each class contains implementations followed by tests. `TestSupport.java` supplies checks and test-only helpers; `AllTests.java` runs the topic.

## Which technique fits?

| Situation | Java file | Variants | Test groups |
|---|---|---:|---:|
| Unsorted pair sums, duplicate checks, first unique/repeating, bijections, intersections | [HashLookup.java](HashLookup.java) | 11 | 8 |
| Anagrams, ransom notes, grouping by a canonical key, marking values in 1..n | [FrequencySignatures.java](FrequencySignatures.java) | 8 | 7 |
| Consecutive runs, smallest missing positive, Sudoku validation, seen-state cycles | [SetSequences.java](SetSequences.java) | 5 | 6 |
| Values appearing more than n/2 or n/3 times | [MajorityVote.java](MajorityVote.java) | 3 | 4 |
| Word reversal, run compression, string encoding, common prefix, zigzag | [StringBuilding.java](StringBuilding.java) | 8 | 7 |
| Longest palindromic substring, palindrome counts, Manacher's algorithm | [ExpandCentre.java](ExpandCentre.java) | 3 | 5 |
| Rabin–Karp, KMP, repeated substrings, shortest palindrome | [StringMatching.java](StringMatching.java) | 7 | 6 |
| **Total** | | **45** | **43** |

The variant count excludes convenience overloads, constructors, private helpers, and test runners.

## Learn in this order

1. **Hash lookups:** decide what information a future item needs. Use a set for existence, a map to remember an index, and a frequency map for counts.
2. **Frequency signatures:** identify what makes two inputs equivalent. Equal anagram signatures belong in the same group. In Java, arrays need a value-based representation before they can serve as grouping keys.
3. **Set sequences:** avoid repeating work by starting only at the beginning of a consecutive run. Learn how array positions can encode membership when values are in a bounded range.
4. **Majority voting:** understand cancellation, then verify the remaining candidates. A candidate is not proof that a majority exists.
5. **String building:** use `StringBuilder`, read/write pointers, and length prefixes. Separate input traversal from output construction.
6. **Palindrome expansion:** handle both odd and even centers. Learn the expansion approach first; use Manacher as the advanced linear-time variant.
7. **String matching:** understand a rolling hash and collision verification, then the KMP prefix table and fallback step.

## Method reference

| Class | Public algorithm methods |
|---|---|
| `HashLookup` | `twoSum`, `countPairs`, `containsDuplicate`, `containsNearbyDuplicate`, `firstUniqueChar`, `firstRepeating`, `isIsomorphic`, `wordPattern`, `firstOccurrenceSignature`, `intersection`, `intersectWithCounts` |
| `FrequencySignatures` | `isAnagram`, `canConstruct`, `minStepsToAnagram`, `groupAnagramsSorted`, `groupAnagramsCount`, `groupShifted`, `findDuplicates`, `findDisappeared` |
| `SetSequences` | `longestConsecutive`, `longestConsecutiveRuns`, `firstMissingPositive`, `isValidSudoku`, `isHappy` |
| `MajorityVote` | `majorityCounter`, `majorityVote`, `majorityII` |
| `StringBuilding` | `reverseWords`, `reverseWordsInPlace`, `compress`, `compressString`, `encode`, `decode`, `longestCommonPrefix`, `zigzagConvert` |
| `ExpandCentre` | `longestPalindrome`, `countPalindromes`, `longestPalindromeManacher` |
| `StringMatching` | `rabinKarp`, `repeatedDna`, `longestDupSubstring`, `prefixFunction`, `kmpFindAll`, `repeatedSubstringPattern`, `shortestPalindrome` |

## Java conventions

- Inputs must be non-null unless a method explicitly says otherwise. An empty array/list/string is supported where meaningful.
- Java method names use `camelCase`; classes use `PascalCase`. All algorithms are static methods in package `hashing_strings`.
- `twoSum` accepts a `long` target. Pair sums and complements are evaluated without 32-bit overflow. `countPairs` and `countPalindromes` return `long`.
- No-result conventions follow the source: an empty array/list, `-1`, or boxed `Integer` value `null`, depending on the method. Check `firstRepeating`, `majorityCounter`, and `majorityVote` for null before unboxing.
- String indices, lengths, and character operations use Java **UTF-16 `char` units**. Use ASCII or BMP characters for character-based reversal and palindrome exercises. These methods do not implement Unicode grapheme handling. Supplementary characters may occupy two units; this differs from Python's code-point indexing.
- `encode`/`decode` round-trip arbitrary Java strings, including supplementary characters. Their wire lengths count UTF-16 units, so encodings containing supplementary characters are not directly interchangeable with the Python codec.
- `groupAnagramsCount` and `groupShifted` require lowercase `a`–`z`. Other frequency methods accept all Java `char` values under the UTF-16 convention. Group ordering is not part of the contract.
- `findDuplicates` requires values in `1..n`, each occurring at most twice. `findDisappeared` requires values in `1..n`. Both restore valid input before returning.
- `firstOccurrenceSignature` assigns consecutive IDs in order of first appearance: `paper` becomes `[0, 1, 0, 2, 3]`. It does not return literal positions from the original sequence.
- `isValidSudoku` requires a 9×9 board with `'.'` or `'1'` through `'9'`; malformed shapes/symbols return false. `isHappy` returns false for non-positive integers.
- `minStepsToAnagram` rejects unequal lengths; `zigzagConvert` requires positive rows; `repeatedDna` requires positive window length. The decoder rejects malformed or truncated input with `IllegalArgumentException`.

### Input mutation

| Method | Input behavior |
|---|---|
| `firstMissingPositive` | Rearranges the input array permanently; pass `a.clone()` to preserve it |
| `reverseWordsInPlace` | Rewrites the supplied `char[]` |
| `compress` | Rewrites the supplied `char[]`; only the returned-length prefix is valid output |
| `findDuplicates`, `findDisappeared` | Temporarily negate entries, then restore the original array |
| All other public algorithms | Leave their inputs unchanged |

### Adaptation details

The port uses `HashMap`, `HashSet`, `ArrayList`, and `StringBuilder` from the Java standard library. General algorithms remain close to the Python originals while using explicit Java types and bounds.

Rolling hashes use modulus `1_000_000_007` with `long` intermediates so multiplication fits safely. Matching hash values are always checked against actual text. Manacher and shortest-palindrome construction use negative integer separators, which cannot collide with actual characters such as `#`, `^`, or `$`.

## Run the topic or one family

From the repository root:

```bash
bash run-tests.sh
bash run-tests.sh HashLookup
bash run-tests.sh FrequencySignatures StringMatching
```

After compilation, a family can also run directly:

```bash
java -cp out hashing_strings.HashLookup
```

To reuse an algorithm in your own Java class, import its class, then call it normally:

```java
import hashing_strings.HashLookup;
import hashing_strings.ExpandCentre;

// Inside your method:
int[] pair = HashLookup.twoSum(new int[]{2, 7, 11, 15}, 9); // [0, 1]
String palindrome = ExpandCentre.longestPalindrome("babad"); // "bab"
```

The [original topic](https://github.com/hiepnnguyentcu/dsa-grand-slam/tree/efdf8f18e5917df2db9e51193e2fb7f066c099d8/hashing_strings) links to an external [Hashing & Strings Field Guide](https://claude.ai/code/artifact/124c1e02-f09b-486f-be10-642bb2d8b633). That guide is an original resource, not a converted Java artifact. See [attribution](../ATTRIBUTION.md).
