/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the SearchHistory class.
 */
public class SearchHistoryTest {

    private SearchHistory<String> searchHistory;
    private final String TEST_HISTORY_NAME = "test.search.history";

    @Before
    public void setUp() {
        searchHistory = new SearchHistory<>(TEST_HISTORY_NAME, String.class, 5);
        // Clear any existing test data
        searchHistory.clear();
    }

    @After
    public void tearDown() {
        // Clean up test data
        if (searchHistory != null) {
            searchHistory.clear();
        }
    }

    @Test
    public void testAddSearchEntry() {
        assertTrue("History should be empty initially", searchHistory.isEmpty());
        assertEquals("Size should be 0 initially", 0, searchHistory.size());

        searchHistory.addSearchEntry("test entry 1");
        assertEquals("Size should be 1 after adding entry", 1, searchHistory.size());
        assertFalse("History should not be empty", searchHistory.isEmpty());
        assertEquals("Most recent entry should be the added entry", "test entry 1", searchHistory.getMostRecentEntry());

        searchHistory.addSearchEntry("test entry 2");
        assertEquals("Size should be 2 after adding second entry", 2, searchHistory.size());
        assertEquals("Most recent entry should be the second entry", "test entry 2",
                searchHistory.getMostRecentEntry());

        List<String> entries = searchHistory.getSearchEntries();
        assertEquals("First entry should be most recent", "test entry 2", entries.get(0));
        assertEquals("Second entry should be first added", "test entry 1", entries.get(1));
    }

    @Test
    public void testDuplicateHandling() {
        searchHistory.addSearchEntry("entry 1");
        searchHistory.addSearchEntry("entry 2");
        searchHistory.addSearchEntry("entry 3");

        assertEquals("Should have 3 entries", 3, searchHistory.size());

        // Add duplicate - should move to front
        searchHistory.addSearchEntry("entry 1");
        assertEquals("Should still have 3 entries", 3, searchHistory.size());
        assertEquals("Duplicate should be at front", "entry 1", searchHistory.getMostRecentEntry());

        List<String> entries = searchHistory.getSearchEntries();
        assertEquals("Order should be: entry 1, entry 3, entry 2", "entry 1", entries.get(0));
        assertEquals("Order should be: entry 1, entry 3, entry 2", "entry 3", entries.get(1));
        assertEquals("Order should be: entry 1, entry 3, entry 2", "entry 2", entries.get(2));
    }

    @Test
    public void testMaxEntriesLimit() {
        // Add more entries than the limit (5)
        for (int i = 1; i <= 7; i++) {
            searchHistory.addSearchEntry("entry " + i);
        }

        assertEquals("Should have max entries (5)", 5, searchHistory.size());
        assertEquals("Most recent should be entry 7", "entry 7", searchHistory.getMostRecentEntry());

        List<String> entries = searchHistory.getSearchEntries();
        // Should have entries 7, 6, 5, 4, 3 (oldest entries 1, 2 removed)
        assertEquals("Should have entry 7", "entry 7", entries.get(0));
        assertEquals("Should have entry 6", "entry 6", entries.get(1));
        assertEquals("Should have entry 5", "entry 5", entries.get(2));
        assertEquals("Should have entry 4", "entry 4", entries.get(3));
        assertEquals("Should have entry 3", "entry 3", entries.get(4));
    }

    @Test
    public void testDeleteSearchEntry() {
        searchHistory.addSearchEntry("entry 1");
        searchHistory.addSearchEntry("entry 2");
        searchHistory.addSearchEntry("entry 3");

        assertTrue("Should contain entry 2", searchHistory.containsEntry("entry 2"));
        boolean removed = searchHistory.deleteSearchEntry("entry 2");
        assertTrue("Should return true when entry is removed", removed);
        assertEquals("Should have 2 entries after removal", 2, searchHistory.size());
        assertFalse("Should not contain entry 2 after removal", searchHistory.containsEntry("entry 2"));

        boolean removedAgain = searchHistory.deleteSearchEntry("entry 2");
        assertFalse("Should return false when trying to remove non-existent entry", removedAgain);
    }

    @Test
    public void testDeleteByIndex() {
        searchHistory.addSearchEntry("entry 1");
        searchHistory.addSearchEntry("entry 2");
        searchHistory.addSearchEntry("entry 3");

        // Delete middle entry (index 1, which should be "entry 2")
        String removed = searchHistory.deleteSearchEntry(1);
        assertEquals("Should remove entry 2", "entry 2", removed);
        assertEquals("Should have 2 entries after removal", 2, searchHistory.size());

        List<String> entries = searchHistory.getSearchEntries();
        assertEquals("Should have entry 3", "entry 3", entries.get(0));
        assertEquals("Should have entry 1", "entry 1", entries.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteByInvalidIndex() {
        searchHistory.addSearchEntry("entry 1");
        searchHistory.deleteSearchEntry(5); // Should throw exception
    }

    @Test
    public void testUpdateSearchEntry() {
        searchHistory.addSearchEntry("entry 1");
        searchHistory.addSearchEntry("entry 2");
        searchHistory.addSearchEntry("entry 3");

        // Update existing entry
        searchHistory.updateSearchEntry("entry 2", "updated entry 2");
        assertEquals("Should still have 3 entries", 3, searchHistory.size());

        List<String> entries = searchHistory.getSearchEntries();
        assertTrue("Should contain updated entry", entries.contains("updated entry 2"));
        assertFalse("Should not contain old entry", entries.contains("entry 2"));

        // Update non-existing entry (should add to front)
        searchHistory.updateSearchEntry("non-existent", "new entry");
        assertEquals("Should have 4 entries", 4, searchHistory.size());
        assertEquals("New entry should be most recent", "new entry", searchHistory.getMostRecentEntry());
    }

    @Test
    public void testGetSearchEntriesWithLimit() {
        for (int i = 1; i <= 5; i++) {
            searchHistory.addSearchEntry("entry " + i);
        }

        List<String> limited = searchHistory.getSearchEntries(3);
        assertEquals("Should return 3 entries", 3, limited.size());
        assertEquals("Should have most recent entries", "entry 5", limited.get(0));
        assertEquals("Should have most recent entries", "entry 4", limited.get(1));
        assertEquals("Should have most recent entries", "entry 3", limited.get(2));

        List<String> overLimit = searchHistory.getSearchEntries(10);
        assertEquals("Should return all 5 entries when limit exceeds size", 5, overLimit.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSearchEntriesWithNegativeLimit() {
        searchHistory.getSearchEntries(-1);
    }

    @Test
    public void testClear() {
        searchHistory.addSearchEntry("entry 1");
        searchHistory.addSearchEntry("entry 2");
        assertFalse("Should not be empty before clear", searchHistory.isEmpty());

        searchHistory.clear();
        assertTrue("Should be empty after clear", searchHistory.isEmpty());
        assertEquals("Size should be 0 after clear", 0, searchHistory.size());
        assertNull("Most recent entry should be null after clear", searchHistory.getMostRecentEntry());
    }

    @Test
    public void testPersistence() {
        // Add entries to first instance
        searchHistory.addSearchEntry("persistent entry 1");
        searchHistory.addSearchEntry("persistent entry 2");

        // Create new instance with same name - should load existing data
        SearchHistory<String> newHistory = new SearchHistory<>(TEST_HISTORY_NAME, String.class, 5);
        assertEquals("New instance should load existing data", 2, newHistory.size());
        assertEquals("Should have same most recent entry", "persistent entry 2", newHistory.getMostRecentEntry());
        assertTrue("Should contain first entry", newHistory.containsEntry("persistent entry 1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullHistoryName() {
        new SearchHistory<>(null, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyHistoryName() {
        new SearchHistory<>("", String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEntryType() {
        new SearchHistory<>("test", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroMaxEntries() {
        new SearchHistory<>("test", String.class, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullEntry() {
        searchHistory.addSearchEntry(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithNullNewEntry() {
        searchHistory.updateSearchEntry("old", null);
    }

    @Test
    public void testGetters() {
        assertEquals("History name should match", TEST_HISTORY_NAME, searchHistory.getHistoryName());
        assertEquals("Max entries should match", 5, searchHistory.getMaxEntries());
    }
}
