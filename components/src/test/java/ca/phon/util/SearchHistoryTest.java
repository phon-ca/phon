package ca.phon.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SearchHistory}.
 * Tests cover static API, prefix-based organization, thread safety, and
 * persistence.
 */
@RunWith(JUnit4.class)
public class SearchHistoryTest {

    private static final String TEST_PREFIX = "test.prefix";
    private static final String TEST_PREFIX_2 = "test.prefix2";
    private static final String TEMP_PREFIX = "temp.test";

    @Before
    public void setUp() {
        // Clean up any existing test data
        SearchHistory.clear(TEST_PREFIX);
        SearchHistory.clear(TEST_PREFIX_2);
        SearchHistory.clear(TEMP_PREFIX);
    }

    @After
    public void tearDown() {
        // Clean up test data
        SearchHistory.clear(TEST_PREFIX);
        SearchHistory.clear(TEST_PREFIX_2);
        SearchHistory.clear(TEMP_PREFIX);
        SearchHistory.deleteHistoriesWithPattern("test.");
        SearchHistory.deleteHistoriesWithPattern("temp.");
    }

    @Test
    public void testBasicAddAndRetrieve() {
        assertTrue("History should be empty initially", SearchHistory.isEmpty(TEST_PREFIX));
        assertEquals("Size should be 0 initially", 0, SearchHistory.size(TEST_PREFIX));

        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test query")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, entry);

        assertFalse("History should not be empty after adding entry", SearchHistory.isEmpty(TEST_PREFIX));
        assertEquals("Size should be 1 after adding entry", 1, SearchHistory.size(TEST_PREFIX));

        List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should have one entry", 1, entries.size());
        assertEquals("Entry should match", entry, entries.get(0));
    }

    @Test
    public void testAddMultipleEntries() {
        SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                .queryText("query 1")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                .queryText("query 2")
                .queryType(SearchType.REGEX)
                .build();

        SearchHistoryEntry entry3 = SearchHistoryEntry.builder()
                .queryText("query 3")
                .queryType(SearchType.PLAIN)
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, entry1);
        SearchHistory.addSearchEntry(TEST_PREFIX, entry2);
        SearchHistory.addSearchEntry(TEST_PREFIX, entry3);

        List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should have 3 entries", 3, entries.size());

        // Entries should be in reverse order (most recent first)
        assertEquals("First entry should be most recent", entry3, entries.get(0));
        assertEquals("Second entry should be middle", entry2, entries.get(1));
        assertEquals("Third entry should be oldest", entry1, entries.get(2));
    }

    @Test
    public void testDuplicateRemoval() {
        SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                .queryText("duplicate query")
                .queryType(SearchType.PHONEX)
                .parameter("target", "IPA")
                .build();

        SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                .queryText("other query")
                .queryType(SearchType.REGEX)
                .build();

        // Add different entries
        SearchHistory.addSearchEntry(TEST_PREFIX, entry1);
        SearchHistory.addSearchEntry(TEST_PREFIX, entry2);
        assertEquals("Should have 2 entries", 2, SearchHistory.size(TEST_PREFIX));

        // Add duplicate of entry1 (same query text and parameters)
        SearchHistoryEntry duplicateEntry = SearchHistoryEntry.builder()
                .queryText("duplicate query")
                .queryType(SearchType.PHONEX) // type can be different
                .parameter("target", "IPA")
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, duplicateEntry);

        List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should still have 2 entries after duplicate", 2, entries.size());
        assertEquals("Duplicate should be at front", duplicateEntry, entries.get(0));
        assertEquals("Other entry should remain", entry2, entries.get(1));
    }

    @Test
    public void testMaxEntriesLimit() {
        int maxEntries = 3;

        // Add more entries than the limit
        for (int i = 1; i <= 5; i++) {
            SearchHistoryEntry entry = SearchHistoryEntry.builder()
                    .queryText("query " + i)
                    .queryType(SearchType.PHONEX)
                    .build();
            SearchHistory.addSearchEntry(TEST_PREFIX, entry, maxEntries);
        }

        List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should have max entries", maxEntries, entries.size());

        // Should have the most recent entries (3, 4, 5)
        assertEquals("query 5", entries.get(0).queryText());
        assertEquals("query 4", entries.get(1).queryText());
        assertEquals("query 3", entries.get(2).queryText());
    }

    @Test
    public void testGetLimitedEntries() {
        // Add 5 entries
        for (int i = 1; i <= 5; i++) {
            SearchHistoryEntry entry = SearchHistoryEntry.builder()
                    .queryText("query " + i)
                    .queryType(SearchType.PHONEX)
                    .build();
            SearchHistory.addSearchEntry(TEST_PREFIX, entry);
        }

        List<SearchHistoryEntry> limited = SearchHistory.getSearchEntries(TEST_PREFIX, 3);
        assertEquals("Should return limited number", 3, limited.size());
        assertEquals("query 5", limited.get(0).queryText());
        assertEquals("query 4", limited.get(1).queryText());
        assertEquals("query 3", limited.get(2).queryText());

        // Test limit larger than available
        List<SearchHistoryEntry> all = SearchHistory.getSearchEntries(TEST_PREFIX, 10);
        assertEquals("Should return all available entries", 5, all.size());
    }

    @Test
    public void testMostRecentEntry() {
        assertNull("Most recent should be null for empty history",
                SearchHistory.getMostRecentEntry(TEST_PREFIX));

        SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                .queryText("first")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                .queryText("second")
                .queryType(SearchType.REGEX)
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, entry1);
        assertEquals("Most recent should be first entry", entry1,
                SearchHistory.getMostRecentEntry(TEST_PREFIX));

        SearchHistory.addSearchEntry(TEST_PREFIX, entry2);
        assertEquals("Most recent should be second entry", entry2,
                SearchHistory.getMostRecentEntry(TEST_PREFIX));
    }

    @Test
    public void testDeleteEntry() {
        SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                .queryText("keep this")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                .queryText("delete this")
                .queryType(SearchType.REGEX)
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, entry1);
        SearchHistory.addSearchEntry(TEST_PREFIX, entry2);
        assertEquals("Should have 2 entries", 2, SearchHistory.size(TEST_PREFIX));

        // Delete specific entry
        boolean deleted = SearchHistory.deleteSearchEntry(TEST_PREFIX, entry2);
        assertTrue("Should return true for successful deletion", deleted);
        assertEquals("Should have 1 entry after deletion", 1, SearchHistory.size(TEST_PREFIX));

        List<SearchHistoryEntry> remaining = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should have only first entry", entry1, remaining.get(0));

        // Try to delete non-existent entry
        boolean notDeleted = SearchHistory.deleteSearchEntry(TEST_PREFIX, entry2);
        assertFalse("Should return false for non-existent entry", notDeleted);
    }

    @Test
    public void testDeleteByIndex() {
        SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                .queryText("first")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                .queryText("second")
                .queryType(SearchType.REGEX)
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, entry1);
        SearchHistory.addSearchEntry(TEST_PREFIX, entry2);

        // Delete first entry (most recent)
        SearchHistoryEntry deleted = SearchHistory.deleteSearchEntry(TEST_PREFIX, 0);
        assertEquals("Should return deleted entry", entry2, deleted);
        assertEquals("Should have 1 entry remaining", 1, SearchHistory.size(TEST_PREFIX));

        List<SearchHistoryEntry> remaining = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should have only first entry", entry1, remaining.get(0));

        // Test invalid index
        try {
            SearchHistory.deleteSearchEntry(TEST_PREFIX, 5);
            fail("Should throw exception for invalid index");
        } catch (IndexOutOfBoundsException e) {
            assertTrue(e.getMessage().contains("out of range"));
        }
    }

    @Test
    public void testUpdateEntry() {
        SearchHistoryEntry original = SearchHistoryEntry.builder()
                .queryText("original")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistoryEntry updated = SearchHistoryEntry.builder()
                .queryText("updated")
                .queryType(SearchType.REGEX)
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, original);

        // Update existing entry
        SearchHistory.updateSearchEntry(TEST_PREFIX, original, updated);

        List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should have 1 entry", 1, entries.size());
        assertEquals("Entry should be updated", updated, entries.get(0));

        // Update non-existent entry (should add to front)
        SearchHistoryEntry nonExistent = SearchHistoryEntry.builder()
                .queryText("non-existent")
                .queryType(SearchType.PLAIN)
                .build();

        SearchHistoryEntry newEntry = SearchHistoryEntry.builder()
                .queryText("new entry")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistory.updateSearchEntry(TEST_PREFIX, nonExistent, newEntry);

        entries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should have 2 entries", 2, entries.size());
        assertEquals("New entry should be at front", newEntry, entries.get(0));
        assertEquals("Updated entry should be second", updated, entries.get(1));
    }

    @Test
    public void testContainsEntry() {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test")
                .queryType(SearchType.PHONEX)
                .build();

        assertFalse("Should not contain entry initially",
                SearchHistory.containsEntry(TEST_PREFIX, entry));

        SearchHistory.addSearchEntry(TEST_PREFIX, entry);

        assertTrue("Should contain entry after adding",
                SearchHistory.containsEntry(TEST_PREFIX, entry));

        SearchHistory.deleteSearchEntry(TEST_PREFIX, entry);

        assertFalse("Should not contain entry after deletion",
                SearchHistory.containsEntry(TEST_PREFIX, entry));
    }

    @Test
    public void testFindMethods() {
        SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                .queryText("phoneme pattern")
                .queryType(SearchType.PHONEX)
                .parameter("target", "IPA Target")
                .build();

        SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                .queryText("regex pattern")
                .queryType(SearchType.REGEX)
                .parameter("group", "Word")
                .build();

        SearchHistoryEntry entry3 = SearchHistoryEntry.builder()
                .queryText("phoneme pattern")
                .queryType(SearchType.PLAIN)
                .parameter("target", "Orthography")
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, entry1);
        SearchHistory.addSearchEntry(TEST_PREFIX, entry2);
        SearchHistory.addSearchEntry(TEST_PREFIX, entry3);

        // Test find by query text
        List<SearchHistoryEntry> byQueryText = SearchHistory.findEntriesByQueryText(
                TEST_PREFIX, "phoneme pattern");
        assertEquals("Should find 2 entries with same query text", 2, byQueryText.size());
        assertTrue(byQueryText.contains(entry1));
        assertTrue(byQueryText.contains(entry3));

        // Test find by query type
        List<SearchHistoryEntry> byQueryType = SearchHistory.findEntriesByQueryType(
                TEST_PREFIX, SearchType.PHONEX);
        assertEquals("Should find 1 entry with phonex type", 1, byQueryType.size());
        assertEquals(entry1, byQueryType.get(0));

        // Test find by parameter key
        List<SearchHistoryEntry> byParameter = SearchHistory.findEntriesByParameter(
                TEST_PREFIX, "target");
        assertEquals("Should find 2 entries with target parameter", 2, byParameter.size());
        assertTrue(byParameter.contains(entry1));
        assertTrue(byParameter.contains(entry3));

        // Test find by parameter value
        List<SearchHistoryEntry> byParameterValue = SearchHistory.findEntriesByParameterValue(
                TEST_PREFIX, "target", "IPA Target");
        assertEquals("Should find 1 entry with specific parameter value", 1, byParameterValue.size());
        assertEquals(entry1, byParameterValue.get(0));

        // Test with null values
        assertTrue("Should return empty list for null query text",
                SearchHistory.findEntriesByQueryText(TEST_PREFIX, null).isEmpty());
        assertTrue("Should return empty list for null query type",
                SearchHistory.findEntriesByQueryType(TEST_PREFIX, null).isEmpty());
        assertTrue("Should return empty list for null parameter key",
                SearchHistory.findEntriesByParameter(TEST_PREFIX, null).isEmpty());
        assertTrue("Should return empty list for null parameter value",
                SearchHistory.findEntriesByParameterValue(TEST_PREFIX, "key", null).isEmpty());
    }

    @Test
    public void testSimpleSearchEntry() {
        // Test basic simple entry
        SearchHistoryEntry entry1 = SearchHistory.addSimpleSearchEntry(
                TEST_PREFIX, "simple query", SearchType.PHONEX);

        assertEquals("simple query", entry1.queryText());
        assertEquals(SearchType.PHONEX, entry1.queryType());
        assertFalse(entry1.caseSensitive());
        assertTrue(entry1.parameters().isEmpty());

        // Test with case sensitivity
        SearchHistoryEntry entry2 = SearchHistory.addSimpleSearchEntry(
                TEST_PREFIX, "case sensitive", SearchType.REGEX, true);

        assertEquals("case sensitive", entry2.queryText());
        assertEquals(SearchType.REGEX, entry2.queryType());
        assertTrue(entry2.caseSensitive());

        // Verify both entries are in history
        List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Should have 2 entries", 2, entries.size());
        assertEquals("Most recent should be first", entry2, entries.get(0));
        assertEquals("Older should be second", entry1, entries.get(1));
    }

    @Test
    public void testPrefixSeparation() {
        SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                .queryText("prefix1 query")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                .queryText("prefix2 query")
                .queryType(SearchType.REGEX)
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, entry1);
        SearchHistory.addSearchEntry(TEST_PREFIX_2, entry2);

        // Each prefix should have its own entry
        List<SearchHistoryEntry> entries1 = SearchHistory.getSearchEntries(TEST_PREFIX);
        List<SearchHistoryEntry> entries2 = SearchHistory.getSearchEntries(TEST_PREFIX_2);

        assertEquals("Prefix 1 should have 1 entry", 1, entries1.size());
        assertEquals("Prefix 2 should have 1 entry", 1, entries2.size());
        assertEquals("Prefix 1 entry should match", entry1, entries1.get(0));
        assertEquals("Prefix 2 entry should match", entry2, entries2.get(0));

        // Clear one prefix shouldn't affect the other
        SearchHistory.clear(TEST_PREFIX);

        assertTrue("Prefix 1 should be empty", SearchHistory.isEmpty(TEST_PREFIX));
        assertFalse("Prefix 2 should not be empty", SearchHistory.isEmpty(TEST_PREFIX_2));
    }

    @Test
    public void testBulkOperations() {
        // Add entries to multiple prefixes
        SearchHistory.addSimpleSearchEntry("test.query.phonex", "phonex1", SearchType.PHONEX);
        SearchHistory.addSimpleSearchEntry("test.query.regex", "regex1", SearchType.REGEX);
        SearchHistory.addSimpleSearchEntry("test.analysis.segment", "segment1", SearchType.PLAIN);
        SearchHistory.addSimpleSearchEntry("temp.test.data", "temp1", SearchType.PHONEX);

        // Test pattern matching
        List<String> testPrefixes = SearchHistory.getHistoryPrefixesWithPattern("test.");
        assertTrue("Should find test prefixes", testPrefixes.size() >= 3);
        assertTrue("Should contain phonex prefix", testPrefixes.contains("test.query.phonex"));
        assertTrue("Should contain regex prefix", testPrefixes.contains("test.query.regex"));
        assertTrue("Should contain analysis prefix", testPrefixes.contains("test.analysis.segment"));

        List<String> queryPrefixes = SearchHistory.getHistoryPrefixesWithPattern("test.query.");
        assertEquals("Should find 2 query prefixes", 2, queryPrefixes.size());

        // Test existence checks
        assertTrue("Should exist with test pattern",
                SearchHistory.existsHistoryWithPattern("test."));
        assertFalse("Should not exist with nonexistent pattern",
                SearchHistory.existsHistoryWithPattern("nonexistent."));

        // Test count
        int testCount = SearchHistory.getHistoryCountWithPattern("test.");
        assertTrue("Should have test histories", testCount >= 3);

        // Test bulk delete
        int deleted = SearchHistory.deleteHistoriesWithPattern("test.query.");
        assertEquals("Should delete 2 query histories", 2, deleted);

        // Verify deletion
        assertFalse("Query phonex should be gone",
                SearchHistory.existsHistoryWithPattern("test.query.phonex"));
        assertFalse("Query regex should be gone",
                SearchHistory.existsHistoryWithPattern("test.query.regex"));
        assertTrue("Analysis should remain",
                SearchHistory.existsHistoryWithPattern("test.analysis."));
        assertTrue("Temp should remain",
                SearchHistory.existsHistoryWithPattern("temp."));
    }

    @Test
    public void testGetAllHistoryPrefixes() {
        SearchHistory.addSimpleSearchEntry("test.one", "query1", SearchType.PHONEX);
        SearchHistory.addSimpleSearchEntry("test.two", "query2", SearchType.REGEX);
        SearchHistory.addSimpleSearchEntry("other.prefix", "query3", SearchType.PLAIN);

        List<String> allPrefixes = SearchHistory.getAllHistoryPrefixes();
        assertTrue("Should contain test.one", allPrefixes.contains("test.one"));
        assertTrue("Should contain test.two", allPrefixes.contains("test.two"));
        assertTrue("Should contain other.prefix", allPrefixes.contains("other.prefix"));
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        int numThreads = 10;
        int entriesPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Submit tasks that add entries concurrently
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < entriesPerThread; i++) {
                        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText("thread" + threadId + "_entry" + i)
                                .queryType(SearchType.PHONEX)
                                .build();
                        SearchHistory.addSearchEntry(TEST_PREFIX, entry);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Verify all entries were added (subject to max entries limit)
        List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertTrue("Should have entries from concurrent operations", entries.size() > 0);
        assertTrue("Should not exceed default max entries",
                entries.size() <= SearchHistory.DEFAULT_MAX_ENTRIES);

        // Verify no corruption occurred
        for (SearchHistoryEntry entry : entries) {
            assertNotNull("Entry should not be null", entry);
            assertNotNull("Query text should not be null", entry.queryText());
            assertNotNull("Query type should not be null", entry.queryType());
        }
    }

    @Test
    public void testValidationErrors() {
        // Test null prefix
        try {
            SearchHistory.addSearchEntry(null, SearchHistoryEntry.builder()
                    .queryText("test")
                    .queryType(SearchType.PHONEX)
                    .build());
            fail("Should throw exception for null prefix");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be null or empty"));
        }

        // Test empty prefix
        try {
            SearchHistory.addSearchEntry("  ", SearchHistoryEntry.builder()
                    .queryText("test")
                    .queryType(SearchType.PHONEX)
                    .build());
            fail("Should throw exception for empty prefix");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be null or empty"));
        }

        // Test null entry
        try {
            SearchHistory.addSearchEntry(TEST_PREFIX, null);
            fail("Should throw exception for null entry");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Search entry cannot be null"));
        }

        // Test invalid max entries
        try {
            SearchHistory.addSearchEntry(TEST_PREFIX, SearchHistoryEntry.builder()
                    .queryText("test")
                    .queryType(SearchType.PHONEX)
                    .build(), 0);
            fail("Should throw exception for zero max entries");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Max entries must be greater than 0"));
        }

        // Test negative limit for getSearchEntries
        try {
            SearchHistory.getSearchEntries(TEST_PREFIX, -1);
            fail("Should throw exception for negative limit");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Limit cannot be negative"));
        }
    }

    @Test
    public void testClearHistory() {
        // Add some entries
        SearchHistory.addSimpleSearchEntry(TEST_PREFIX, "query1", SearchType.PHONEX);
        SearchHistory.addSimpleSearchEntry(TEST_PREFIX, "query2", SearchType.REGEX);

        assertFalse("History should not be empty", SearchHistory.isEmpty(TEST_PREFIX));
        assertEquals("Should have 2 entries", 2, SearchHistory.size(TEST_PREFIX));

        // Clear history
        SearchHistory.clear(TEST_PREFIX);

        assertTrue("History should be empty after clear", SearchHistory.isEmpty(TEST_PREFIX));
        assertEquals("Size should be 0 after clear", 0, SearchHistory.size(TEST_PREFIX));
        assertNull("Most recent should be null after clear",
                SearchHistory.getMostRecentEntry(TEST_PREFIX));
    }

    @Test
    public void testImmutableReturnValues() {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test")
                .queryType(SearchType.PHONEX)
                .build();

        SearchHistory.addSearchEntry(TEST_PREFIX, entry);

        // Get entries and try to modify the returned list
        List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries(TEST_PREFIX);

        // The returned list should be modifiable (it's a copy)
        entries.clear();

        // Original history should be unaffected
        assertEquals("Original history should be unchanged", 1, SearchHistory.size(TEST_PREFIX));

        // Get fresh copy to verify
        List<SearchHistoryEntry> freshEntries = SearchHistory.getSearchEntries(TEST_PREFIX);
        assertEquals("Fresh copy should have original entry", 1, freshEntries.size());
        assertEquals("Entry should match original", entry, freshEntries.get(0));
    }
}
