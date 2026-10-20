package hashing_strings;

import java.util.*;

/** Run all seven families, or pass class names to run selected families. */
public final class AllTests {
    private AllTests() {}

    public static void main(String[] args) {
        Map<String, Runnable> suites = new LinkedHashMap<>();
        suites.put("HashLookup", HashLookup::runTests);
        suites.put("FrequencySignatures", FrequencySignatures::runTests);
        suites.put("SetSequences", SetSequences::runTests);
        suites.put("MajorityVote", MajorityVote::runTests);
        suites.put("StringBuilding", StringBuilding::runTests);
        suites.put("ExpandCentre", ExpandCentre::runTests);
        suites.put("StringMatching", StringMatching::runTests);
        List<String> selected = args.length == 0 ? new ArrayList<>(suites.keySet()) : Arrays.asList(args);
        for (String name : selected) if (!suites.containsKey(name))
            throw new IllegalArgumentException("Unknown family: " + name + ". Choose " + suites.keySet());
        for (String name : selected) { System.out.println("\n" + name); suites.get(name).run(); }
        TestSupport.summary();
    }
}
