# DSA Grand Slam — Java

A topic-by-topic Java adaptation of [hiepnnguyentcu/dsa-grand-slam](https://github.com/hiepnnguyentcu/dsa-grand-slam), prepared for [phuthanh03012003](https://github.com/phuthanh03012003).

**Topic 1: Hashing & Strings is complete.** It contains all 45 public algorithm variants from the seven original Python technique files, expressed as Java methods. Each class includes explanations, examples, and tests. The remaining topics have not been converted yet.

## Run

Install a **JDK 17 or newer**. No Maven, Gradle, JUnit, or downloaded dependencies are required.

From this directory on macOS/Linux or Git Bash:

```bash
bash run-tests.sh
```

Run only one family:

```bash
bash run-tests.sh HashLookup
```

Or compile and run manually:

```bash
mkdir -p out
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d out hashing_strings/*.java
java -cp out hashing_strings.AllTests
java -cp out hashing_strings.HashLookup
```

The script can also invoke the JDK compiler module when `javac` is absent from PATH. A Java installation without the compiler module requires installing a full JDK.

## Study map

| Order | Topic | Status |
|---|---|---|
| 1 | [Hashing & Strings](hashing_strings/README.md) | Complete: 7 families, 45 algorithm variants |
| 2 | Sliding Window & Two Pointers | Not yet converted |
| 3 | Prefix Sums | Not yet converted |
| Later | Sorting, intervals, binary search, linked lists, stacks/queues/heaps, matrices, trees, BSTs, tries, backtracking, greedy, graphs, DP, bits, math, data structure design | Not yet converted |

Start with `HashLookup.java`. Read its class comment and method explanation, try implementing the idea yourself, then compare with the implementation and run its tests. Continue in the order in the topic guide.

## Testing

The first topic was compiled with Java 17, with compiler warnings treated as errors. All **43 test groups and 21,551 checks passed**. These include the 36 original test themes adapted to Java, plus seven Java-focused regression groups.

The tests use fixed random seeds and independent reference checks: brute-force pair enumeration, sorted/counting comparisons, Sudoku unit scans, regex run grouping, grid simulation, exhaustive substring checks, and Java's substring search. Additional checks cover integer limits, large pair counts, malformed encodings, rolling-hash collisions, and delimiter characters.

Checks throw `AssertionError` directly, so tests remain active without the JVM's `-ea` flag. Any failure exits with an error. `out/` contains generated classes and is ignored by Git.

See [the topic guide](hashing_strings/README.md) for Java-specific contracts and [attribution](ATTRIBUTION.md) for the source revision.
# DSA-Private
