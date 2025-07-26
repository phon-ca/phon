package ca.phon.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Map;

/**
 * Test class for SearchHistory
 */
public class SearchHistoryTest {

    private SearchHistory searchHistory;
    private SearchHistoryEntry testEntry1;
    private SearchHistoryEntry testEntry2;
    private SearchHistoryEntry testEntry3;
    private String testHistoryName;

    @Before
    public void setUp() {
        testHistoryName = "test-history-" + System.currentTimeMillis();
        searchHistory = new SearchHistory(testHistoryName);

        testEntry1 = SearchHistoryEntry.builder()
                .queryText("query1")
                .queryType("phonex")
                .caseSensitive(false)
                .parameter("target", "IPA Target")
                .build();

        testEntry2 = SearchHistoryEntry.builder()
                .queryText("query2")
                .queryType("regex")
                .caseSensitive(true)
                .parameter("group", "Word")
                .build();

        testEntry3 = SearchHistoryEntry.builder()
                .queryText("query3")
                .queryType("plain")
                .build();
    }

    @After
    public void tearDown() {
        // Clean up test data
        if (searchHistory != null) {
            searchHistory.clear();
        }
        // Clean up any histories created during static method tests
        SearchHistory.deleteHistoriesWithPrefix("test-");
    }

    @Test
    public void testConstructorDefault() {
        SearchHistory history = new SearchHistory("test-default");
        assertEquals("test-default", history.getHistoryName());
        assertEquals(SearchHistory.DEFAULT_MAX_ENTRIES, history.getMaxEntries());
        assertTrue(history.isEmpty());
        assertEquals(0, history.size());
    }

    @Test
    public void testConstructorWithMaxEntries() {
        SearchHistory history = new SearchHistory("test-custom", 10);
        assertEquals("test-custom", history.getHistoryName());
        assertEquals(10, history.getMaxEntries());
        assertTrue(history.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullName() {
        new SearchHistory(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyName() {
        new SearchHistory("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroMaxEntries() {
        new SearchHistory("test", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMaxEntries() {
        new SearchHistory("test", -1);
    }

    @Test
    public void testAddSearchEntry() {
        assertTrue(searchHistory.isEmpty());

        searchHistory.addSearchEntry(testEntry1);

        assertEquals(1, searchHistory.size());
        assertFalse(searchHistory.isEmpty());
        assertEquals(testEntry1, searchHistory.getMostRecentEntry());
        assertTrue(searchHistory.containsEntry(testEntry1));
    }

    @Test
    public void testAddMultipleEntries() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.addSearchEntry(testEntry2);
        searchHistory.addSearchEntry(testEntry3);

        assertEquals(3, searchHistory.size());

        List<SearchHistoryEntry> entries = searchHistory.getSearchEntries();
        assertEquals(testEntry3, entries.get(0)); // Most recent first
        assertEquals(testEntry2, entries.get(1));
        assertEquals(testEntry1, entries.get(2));
    }

    @Test
    public void testAddDuplicateEntry() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.addSearchEntry(testEntry2);
        searchHistory.addSearchEntry(testEntry1); // Add duplicate

        assertEquals(2, searchHistory.size()); // Should not increase size
        assertEquals(testEntry1, searchHistory.getMostRecentEntry()); // Should move to front

        List<SearchHistoryEntry> entries = searchHistory.getSearchEntries();
        assertEquals(testEntry1, entries.get(0));
        assertEquals(testEntry2, entries.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullEntry() {
        searchHistory.addSearchEntry(null);
    }

    @Test
    public void testMaxEntriesLimit() {
        SearchHistory smallHistory = new SearchHistory("test-small", 2);

        smallHistory.addSearchEntry(testEntry1);
        smallHistory.addSearchEntry(testEntry2);
        smallHistory.addSearchEntry(testEntry3); // Should remove oldest

        assertEquals(2, smallHistory.size());

        List<SearchHistoryEntry> entries = smallHistory.getSearchEntries();
        assertEquals(testEntry3, entries.get(0));
        assertEquals(testEntry2, entries.get(1));
        assertFalse(smallHistory.containsEntry(testEntry1)); // Should be removed
    }

    @Test
    public void testUpdateSearchEntry() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.addSearchEntry(testEntry2);

        SearchHistoryEntry newEntry = SearchHistoryEntry.builder()
                .queryText("updated query")
                .queryType("phonex")
                .build();

        searchHistory.updateSearchEntry(testEntry1, newEntry);

        assertEquals(2, searchHistory.size());
        assertFalse(searchHistory.containsEntry(testEntry1));
        assertTrue(searchHistory.containsEntry(newEntry));

        List<SearchHistoryEntry> entries = searchHistory.getSearchEntries();
        assertEquals(testEntry2, entries.get(0)); // Should maintain order
        assertEquals(newEntry, entries.get(1));
    }

    @Test
    public void testUpdateNonExistentEntry() {
        searchHistory.addSearchEntry(testEntry1);

        SearchHistoryEntry newEntry = SearchHistoryEntry.builder()
                .queryText("new query")
                .queryType("phonex")
                .build();

        searchHistory.updateSearchEntry(testEntry2, newEntry); // testEntry2 not in history

        assertEquals(2, searchHistory.size());
        assertEquals(newEntry, searchHistory.getMostRecentEntry()); // Should add to front
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithNullNewEntry() {
        searchHistory.updateSearchEntry(testEntry1, null);
    }

    @Test
    public void testDeleteSearchEntry() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.addSearchEntry(testEntry2);

        boolean removed = searchHistory.deleteSearchEntry(testEntry1);

        assertTrue(removed);
        assertEquals(1, searchHistory.size());
        assertFalse(searchHistory.containsEntry(testEntry1));
        assertTrue(searchHistory.containsEntry(testEntry2));
    }

    @Test
    public void testDeleteNonExistentEntry() {
        searchHistory.addSearchEntry(testEntry1);

        boolean removed = searchHistory.deleteSearchEntry(testEntry2);

        assertFalse(removed);
        assertEquals(1, searchHistory.size());
    }

    @Test
    public void testDeleteSearchEntryByIndex() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.addSearchEntry(testEntry2);
        searchHistory.addSearchEntry(testEntry3);

        SearchHistoryEntry removed = searchHistory.deleteSearchEntry(1); // Remove middle entry

        assertEquals(testEntry2, removed);
        assertEquals(2, searchHistory.size());

        List<SearchHistoryEntry> entries = searchHistory.getSearchEntries();
        assertEquals(testEntry3, entries.get(0));
        assertEquals(testEntry1, entries.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteByInvalidIndex() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.deleteSearchEntry(5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteByNegativeIndex() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.deleteSearchEntry(-1);
    }

    @Test
    public void testGetSearchEntriesWithLimit() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.addSearchEntry(testEntry2);
        searchHistory.addSearchEntry(testEntry3);

        List<SearchHistoryEntry> limited = searchHistory.getSearchEntries(2);

        assertEquals(2, limited.size());
        assertEquals(testEntry3, limited.get(0));
        assertEquals(testEntry2, limited.get(1));
    }

    @Test
    public void testGetSearchEntriesLimitExceedsSize() {
        searchHistory.addSearchEntry(testEntry1);

        List<SearchHistoryEntry> entries = searchHistory.getSearchEntries(10);

        assertEquals(1, entries.size());
        assertEquals(testEntry1, entries.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSearchEntriesNegativeLimit() {
        searchHistory.getSearchEntries(-1);
    }

    @Test
    public void testGetMostRecentEntryEmptyHistory() {
        assertNull(searchHistory.getMostRecentEntry());
    }

    @Test
    public void testFindEntriesByQueryText() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.addSearchEntry(testEntry2);

        SearchHistoryEntry duplicate = SearchHistoryEntry.builder()
                .queryText("query1") // Same as testEntry1
                .queryType("regex")
                .build();
        searchHistory.addSearchEntry(duplicate);

        List<SearchHistoryEntry> found = searchHistory.findEntriesByQueryText("query1");

        assertEquals(2, found.size());
        assertTrue(found.contains(testEntry1));
        assertTrue(found.contains(duplicate));
    }

    @Test
    public void testFindEntriesByQueryTextNotFound() {
        searchHistory.addSearchEntry(testEntry1);

        List<SearchHistoryEntry> found = searchHistory.findEntriesByQueryText("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindEntriesByQueryTextNull() {
        searchHistory.addSearchEntry(testEntry1);

        List<SearchHistoryEntry> found = searchHistory.findEntriesByQueryText(null);

        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindEntriesByQueryType() {
        searchHistory.addSearchEntry(testEntry1); // phonex
        searchHistory.addSearchEntry(testEntry2); // regex
        searchHistory.addSearchEntry(testEntry3); // plain

        SearchHistoryEntry anotherPhonex = SearchHistoryEntry.builder()
                .queryText("another query")
                .queryType("phonex")
                .build();
        searchHistory.addSearchEntry(anotherPhonex);

        List<SearchHistoryEntry> found = searchHistory.findEntriesByQueryType("phonex");

        assertEquals(2, found.size());
        assertTrue(found.contains(testEntry1));
        assertTrue(found.contains(anotherPhonex));
    }

    @Test
    public void testFindEntriesByParameter() {
        searchHistory.addSearchEntry(testEntry1); // has "target" parameter
        searchHistory.addSearchEntry(testEntry2); // has "group" parameter
        searchHistory.addSearchEntry(testEntry3); // no parameters

        List<SearchHistoryEntry> found = searchHistory.findEntriesByParameter("target");

        assertEquals(1, found.size());
        assertEquals(testEntry1, found.get(0));
    }

    @Test
    public void testFindEntriesByParameterValue() {
        searchHistory.addSearchEntry(testEntry1); // target="IPA Target"
        searchHistory.addSearchEntry(testEntry2); // group="Word"

        List<SearchHistoryEntry> found = searchHistory.findEntriesByParameterValue("target", "IPA Target");

        assertEquals(1, found.size());
        assertEquals(testEntry1, found.get(0));
    }

    @Test
    public void testAddSimpleSearchEntry() {
        SearchHistoryEntry entry = searchHistory.addSimpleSearchEntry("simple query", "plain");

        assertEquals("simple query", entry.queryText());
        assertEquals("plain", entry.queryType());
        assertFalse(entry.caseSensitive());
        assertTrue(entry.parameters().isEmpty());
        assertTrue(searchHistory.containsEntry(entry));
    }

    @Test
    public void testAddSimpleSearchEntryWithCaseSensitive() {
        SearchHistoryEntry entry = searchHistory.addSimpleSearchEntry("case query", "regex", true);

        assertEquals("case query", entry.queryText());
        assertEquals("regex", entry.queryType());
        assertTrue(entry.caseSensitive());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSimpleSearchEntryNullQueryText() {
        searchHistory.addSimpleSearchEntry(null, "plain");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSimpleSearchEntryNullQueryType() {
        searchHistory.addSimpleSearchEntry("query", null);
    }

    @Test
    public void testClear() {
        searchHistory.addSearchEntry(testEntry1);
        searchHistory.addSearchEntry(testEntry2);

        assertFalse(searchHistory.isEmpty());

        searchHistory.clear();

        assertTrue(searchHistory.isEmpty());
        assertEquals(0, searchHistory.size());
    }

    @Test
    public void testGetSearchEntriesImmutable() {
        searchHistory.addSearchEntry(testEntry1);

        List<SearchHistoryEntry> entries = searchHistory.getSearchEntries();
        entries.clear(); // Modify returned list

        // Original history should not be affected
        assertEquals(1, searchHistory.size());
    }

    @Test
    public void testGetHistoryNamesWithPrefix() {
        // Create multiple histories
        SearchHistory history1 = new SearchHistory("test-prefix-history1");
        SearchHistory history2 = new SearchHistory("test-prefix-history2");
        SearchHistory history3 = new SearchHistory("other-history");

        // Add entries to make them persistent
        history1.addSearchEntry(testEntry1);
        history2.addSearchEntry(testEntry2);
        history3.addSearchEntry(testEntry3);

        List<String> names = SearchHistory.getHistoryNamesWithPrefix("test-prefix");

        assertTrue(names.contains("test-prefix-history1"));
        assertTrue(names.contains("test-prefix-history2"));
        assertFalse(names.contains("other-history"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetHistoryNamesWithNullPrefix() {
        SearchHistory.getHistoryNamesWithPrefix(null);
    }

    @Test
    public void testGetAllHistoryNames() {
        SearchHistory history1 = new SearchHistory("test-all-1");
        SearchHistory history2 = new SearchHistory("test-all-2");

        history1.addSearchEntry(testEntry1);
        history2.addSearchEntry(testEntry2);

        List<String> allNames = SearchHistory.getAllHistoryNames();

        assertTrue(allNames.contains("test-all-1"));
        assertTrue(allNames.contains("test-all-2"));
    }

    @Test
    public void testGetEntriesFromHistoriesWithPrefix() {
        SearchHistory history1 = new SearchHistory("test-multi-1");
        SearchHistory history2 = new SearchHistory("test-multi-2");

        history1.addSearchEntry(testEntry1);
        history1.addSearchEntry(testEntry2);
        history2.addSearchEntry(testEntry3);

        Map<String, List<SearchHistoryEntry>> entries = SearchHistory.getEntriesFromHistoriesWithPrefix("test-multi");

        assertEquals(2, entries.size());
        assertTrue(entries.containsKey("test-multi-1"));
        assertTrue(entries.containsKey("test-multi-2"));
        assertEquals(2, entries.get("test-multi-1").size());
        assertEquals(1, entries.get("test-multi-2").size());
    }

    @Test
    public void testGetEntriesFromHistoriesWithPrefixAndLimit() {
        SearchHistory history1 = new SearchHistory("test-limit-1");

        history1.addSearchEntry(testEntry1);
        history1.addSearchEntry(testEntry2);
        history1.addSearchEntry(testEntry3);

        Map<String, List<SearchHistoryEntry>> entries = SearchHistory.getEntriesFromHistoriesWithPrefix("test-limit",
                2);

        assertEquals(1, entries.size());
        assertEquals(2, entries.get("test-limit-1").size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEntriesFromHistoriesNullPrefix() {
        SearchHistory.getEntriesFromHistoriesWithPrefix(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEntriesFromHistoriesNegativeLimit() {
        SearchHistory.getEntriesFromHistoriesWithPrefix("test", -1);
    }

    @Test
    public void testDeleteHistoriesWithPrefix() {
        SearchHistory history1 = new SearchHistory("test-delete-1");
        SearchHistory history2 = new SearchHistory("test-delete-2");
        SearchHistory history3 = new SearchHistory("keep-this");

        history1.addSearchEntry(testEntry1);
        history2.addSearchEntry(testEntry2);
        history3.addSearchEntry(testEntry3);

        int deletedCount = SearchHistory.deleteHistoriesWithPrefix("test-delete");

        assertEquals(2, deletedCount);
        assertFalse(SearchHistory.existsHistoryWithPrefix("test-delete"));
        assertTrue(SearchHistory.existsHistoryWithPrefix("keep-this"));
    }

    @Test
    public void testExistsHistoryWithPrefix() {
        assertFalse(SearchHistory.existsHistoryWithPrefix("test-exists"));

        SearchHistory history = new SearchHistory("test-exists-1");
        history.addSearchEntry(testEntry1);

        assertTrue(SearchHistory.existsHistoryWithPrefix("test-exists"));
    }

    @Test
    public void testGetHistoryCountWithPrefix() {
        SearchHistory history1 = new SearchHistory("test-count-1");
        SearchHistory history2 = new SearchHistory("test-count-2");

        history1.addSearchEntry(testEntry1);
        history2.addSearchEntry(testEntry2);

        int count = SearchHistory.getHistoryCountWithPrefix("test-count");

        assertEquals(2, count);
    }

    @Test
    public void testConstructorTrimsName() {
        SearchHistory history = new SearchHistory("  test-name  ");
        assertEquals("test-name", history.getHistoryName());
    }

    @Test
    public void testPersistenceAcrossInstances() {
        String historyName = "test-persistence";

        // Create first instance and add entry
        SearchHistory history1 = new SearchHistory(historyName);
        history1.addSearchEntry(testEntry1);

        // Create second instance with same name
        SearchHistory history2 = new SearchHistory(historyName);

        // Should load the same data
        assertEquals(1, history2.size());
        assertTrue(history2.containsEntry(testEntry1));

        // Clean up
        history2.clear();
    }
}