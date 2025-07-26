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

import java.util.*;
import java.util.prefs.Preferences;

/**
 * <p>
 * Search history manager that stores comprehensive search entries in user
 * preferences.
 * This class provides methods for managing a list of search history entries
 * with support
 * for adding, updating, deleting, and retrieving search history entries.
 * </p>
 * 
 * <p>
 * The search history is stored using the Java Preferences API and persists
 * across application sessions. Each search history instance is identified by
 * a unique name, allowing multiple independent search histories to be
 * maintained.
 * </p>
 * 
 * <p>
 * Each search history entry includes:
 * </p>
 * <ul>
 * <li>Date and time of the search</li>
 * <li>Query text that was searched</li>
 * <li>Query type (e.g., "phonex", "regex", "plain")</li>
 * <li>Case sensitivity setting</li>
 * <li>Optional parameters/filter options</li>
 * </ul>
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Configurable maximum history size with automatic cleanup</li>
 * <li>Most recently used ordering - new entries are added to the front</li>
 * <li>Duplicate detection - existing entries are moved to front when
 * re-added</li>
 * <li>Persistence using Java Preferences API</li>
 * <li>Thread-safe operations</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * SearchHistory queryHistory = new SearchHistory("query.search");
 * SearchHistoryEntry entry = SearchHistoryEntry.builder()
 *         .queryText("phoneme transcription")
 *         .queryType("phonex")
 *         .caseSensitive(false)
 *         .parameter("target", "IPA Target")
 *         .build();
 * queryHistory.addSearchEntry(entry);
 * List&lt;SearchHistoryEntry&gt; recent = queryHistory.getSearchEntries();
 * </pre>
 */
public class SearchHistory {

    /** Default maximum number of search entries to maintain */
    public static final int DEFAULT_MAX_ENTRIES = 20;

    private final String historyName;
    private final int maxEntries;
    private final String prefKey;
    private final Object lock = new Object();

    /**
     * Creates a new search history with the default maximum number of entries.
     * 
     * @param historyName unique name for this search history
     */
    public SearchHistory(String historyName) {
        this(historyName, DEFAULT_MAX_ENTRIES);
    }

    /**
     * Creates a new search history with a specified maximum number of entries.
     * 
     * @param historyName unique name for this search history
     * @param maxEntries  maximum number of entries to maintain (must be > 0)
     * @throws IllegalArgumentException if maxEntries <= 0
     */
    public SearchHistory(String historyName, int maxEntries) {
        if (historyName == null || historyName.trim().isEmpty()) {
            throw new IllegalArgumentException("History name cannot be null or empty");
        }
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("Max entries must be greater than 0");
        }

        this.historyName = historyName.trim();
        this.maxEntries = maxEntries;
        this.prefKey = SearchHistory.class.getName() + "." + this.historyName;
    }

    /**
     * Adds a new search entry to the history. If the entry already exists,
     * it is moved to the front of the list. If the history exceeds the
     * maximum size, the oldest entries are removed.
     * 
     * @param entry the search entry to add (cannot be null)
     * @throws IllegalArgumentException if entry is null
     */
    public void addSearchEntry(SearchHistoryEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Search entry cannot be null");
        }

        synchronized (lock) {
            List<SearchHistoryEntry> entries = getSearchEntries();

            // Remove existing entry if present
            entries.remove(entry);

            // Add to front
            entries.add(0, entry);

            // Trim to max size
            while (entries.size() > maxEntries) {
                entries.remove(entries.size() - 1);
            }

            saveSearchEntries(entries);
        }
    }

    /**
     * Updates an existing search entry. If the old entry exists, it is replaced
     * with the new entry at the same position. If the old entry doesn't exist,
     * the new entry is added to the front.
     * 
     * @param oldEntry the entry to replace
     * @param newEntry the replacement entry (cannot be null)
     * @throws IllegalArgumentException if newEntry is null
     */
    public void updateSearchEntry(SearchHistoryEntry oldEntry, SearchHistoryEntry newEntry) {
        if (newEntry == null) {
            throw new IllegalArgumentException("New search entry cannot be null");
        }

        synchronized (lock) {
            List<SearchHistoryEntry> entries = getSearchEntries();

            int index = entries.indexOf(oldEntry);
            if (index >= 0) {
                // Replace at same position
                entries.set(index, newEntry);
            } else {
                // Add to front if old entry not found
                entries.add(0, newEntry);
                // Trim to max size
                while (entries.size() > maxEntries) {
                    entries.remove(entries.size() - 1);
                }
            }

            saveSearchEntries(entries);
        }
    }

    /**
     * Removes a search entry from the history.
     * 
     * @param entry the entry to remove
     * @return true if the entry was found and removed, false otherwise
     */
    public boolean deleteSearchEntry(SearchHistoryEntry entry) {
        synchronized (lock) {
            List<SearchHistoryEntry> entries = getSearchEntries();
            boolean removed = entries.remove(entry);

            if (removed) {
                saveSearchEntries(entries);
            }

            return removed;
        }
    }

    /**
     * Removes a search entry at the specified index.
     * 
     * @param index the index of the entry to remove
     * @return the removed entry
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public SearchHistoryEntry deleteSearchEntry(int index) {
        synchronized (lock) {
            List<SearchHistoryEntry> entries = getSearchEntries();

            if (index < 0 || index >= entries.size()) {
                throw new IndexOutOfBoundsException("Index " + index + " out of range [0, " + entries.size() + ")");
            }

            SearchHistoryEntry removed = entries.remove(index);
            saveSearchEntries(entries);

            return removed;
        }
    }

    /**
     * Retrieves all search entries in most-recently-used order.
     * The returned list is a copy and modifications will not affect the stored
     * history.
     * 
     * @return a new list containing all search entries (never null)
     */
    public List<SearchHistoryEntry> getSearchEntries() {
        synchronized (lock) {
            return loadSearchEntries();
        }
    }

    /**
     * Retrieves up to the specified number of most recent search entries.
     * 
     * @param limit maximum number of entries to return
     * @return a new list containing the most recent entries (never null)
     * @throws IllegalArgumentException if limit < 0
     */
    public List<SearchHistoryEntry> getSearchEntries(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries();
            int endIndex = Math.min(limit, allEntries.size());
            return new ArrayList<>(allEntries.subList(0, endIndex));
        }
    }

    /**
     * Gets the most recent search entry.
     * 
     * @return the most recent entry, or null if history is empty
     */
    public SearchHistoryEntry getMostRecentEntry() {
        synchronized (lock) {
            List<SearchHistoryEntry> entries = loadSearchEntries();
            return entries.isEmpty() ? null : entries.get(0);
        }
    }

    /**
     * Checks if the history contains the specified entry.
     * 
     * @param entry the entry to check for
     * @return true if the entry exists in the history
     */
    public boolean containsEntry(SearchHistoryEntry entry) {
        synchronized (lock) {
            return loadSearchEntries().contains(entry);
        }
    }

    /**
     * Finds entries that match the given query text.
     * 
     * @param queryText the query text to search for
     * @return a list of matching entries (never null)
     */
    public List<SearchHistoryEntry> findEntriesByQueryText(String queryText) {
        if (queryText == null) {
            return new ArrayList<>();
        }

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries();
            List<SearchHistoryEntry> matches = new ArrayList<>();

            for (SearchHistoryEntry entry : allEntries) {
                if (queryText.equals(entry.getQueryText())) {
                    matches.add(entry);
                }
            }

            return matches;
        }
    }

    /**
     * Finds entries that match the given query type.
     * 
     * @param queryType the query type to search for
     * @return a list of matching entries (never null)
     */
    public List<SearchHistoryEntry> findEntriesByQueryType(String queryType) {
        if (queryType == null) {
            return new ArrayList<>();
        }

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries();
            List<SearchHistoryEntry> matches = new ArrayList<>();

            for (SearchHistoryEntry entry : allEntries) {
                if (queryType.equals(entry.getQueryType())) {
                    matches.add(entry);
                }
            }

            return matches;
        }
    }

    /**
     * Finds entries that contain the specified parameter.
     * 
     * @param parameterKey the parameter key to search for
     * @return a list of matching entries (never null)
     */
    public List<SearchHistoryEntry> findEntriesByParameter(String parameterKey) {
        if (parameterKey == null) {
            return new ArrayList<>();
        }

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries();
            List<SearchHistoryEntry> matches = new ArrayList<>();

            for (SearchHistoryEntry entry : allEntries) {
                if (entry.hasParameter(parameterKey)) {
                    matches.add(entry);
                }
            }

            return matches;
        }
    }

    /**
     * Finds entries that have a specific parameter value.
     * 
     * @param parameterKey   the parameter key
     * @param parameterValue the parameter value to search for
     * @return a list of matching entries (never null)
     */
    public List<SearchHistoryEntry> findEntriesByParameterValue(String parameterKey, String parameterValue) {
        if (parameterKey == null || parameterValue == null) {
            return new ArrayList<>();
        }

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries();
            List<SearchHistoryEntry> matches = new ArrayList<>();

            for (SearchHistoryEntry entry : allEntries) {
                String value = entry.getParameter(parameterKey);
                if (parameterValue.equals(value)) {
                    matches.add(entry);
                }
            }

            return matches;
        }
    }

    /**
     * Adds a simple search entry with just query text and type.
     * This is a convenience method for basic search entries.
     * 
     * @param queryText the query text
     * @param queryType the query type
     * @return the created entry that was added
     * @throws IllegalArgumentException if queryText or queryType is null or empty
     */
    public SearchHistoryEntry addSimpleSearchEntry(String queryText, String queryType) {
        return addSimpleSearchEntry(queryText, queryType, false);
    }

    /**
     * Adds a simple search entry with query text, type, and case sensitivity.
     * This is a convenience method for basic search entries.
     * 
     * @param queryText     the query text
     * @param queryType     the query type
     * @param caseSensitive whether the search is case sensitive
     * @return the created entry that was added
     * @throws IllegalArgumentException if queryText or queryType is null or empty
     */
    public SearchHistoryEntry addSimpleSearchEntry(String queryText, String queryType, boolean caseSensitive) {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText(queryText)
                .queryType(queryType)
                .caseSensitive(caseSensitive)
                .build();

        addSearchEntry(entry);
        return entry;
    }

    /**
     * Gets the current number of entries in the history.
     * 
     * @return the number of entries
     */
    public int size() {
        synchronized (lock) {
            return loadSearchEntries().size();
        }
    }

    /**
     * Checks if the history is empty.
     * 
     * @return true if the history contains no entries
     */
    public boolean isEmpty() {
        synchronized (lock) {
            return loadSearchEntries().isEmpty();
        }
    }

    /**
     * Removes all entries from the search history.
     */
    public void clear() {
        synchronized (lock) {
            Preferences prefs = PrefHelper.getUserPreferences();
            prefs.remove(prefKey);
        }
    }

    /**
     * Gets the maximum number of entries this history will maintain.
     * 
     * @return the maximum number of entries
     */
    public int getMaxEntries() {
        return maxEntries;
    }

    /**
     * Gets the name of this search history.
     * 
     * @return the history name
     */
    public String getHistoryName() {
        return historyName;
    }

    /**
     * Retrieves all search history names that match the given prefix.
     * This is useful for finding related search histories or implementing
     * wildcard-based searches.
     * 
     * @param prefix the prefix to match against history names
     * @return a list of history names that start with the given prefix (never null)
     * @throws IllegalArgumentException if prefix is null
     */
    public static List<String> getHistoryNamesWithPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }

        List<String> matchingNames = new ArrayList<>();
        try {
            Preferences prefs = PrefHelper.getUserPreferences();
            String searchPrefix = SearchHistory.class.getName() + "." + prefix;

            String[] keys = prefs.keys();
            for (String key : keys) {
                if (key.startsWith(searchPrefix)) {
                    // Extract the history name from the full preference key
                    String historyName = key.substring(SearchHistory.class.getName().length() + 1);
                    matchingNames.add(historyName);
                }
            }
        } catch (Exception e) {
            // Log error but return empty list - preference reading should be non-fatal
            java.util.logging.Logger.getLogger(SearchHistory.class.getName())
                    .warning("Failed to retrieve history names with prefix '" + prefix + "': " + e.getMessage());
        }

        return matchingNames;
    }

    /**
     * Retrieves all search history names stored in user preferences.
     * This returns all SearchHistory instances that have been created and have
     * data.
     * 
     * @return a list of all history names (never null)
     */
    public static List<String> getAllHistoryNames() {
        return getHistoryNamesWithPrefix("");
    }

    /**
     * Retrieves search entries from multiple histories that match the given prefix.
     * This is useful for aggregating search results across related histories.
     * 
     * @param prefix               the prefix to match against history names
     * @param maxEntriesPerHistory maximum entries to retrieve from each matching
     *                             history
     * @return a map of history names to their search entries (never null)
     * @throws IllegalArgumentException if prefix is null or maxEntriesPerHistory <
     *                                  0
     */
    public static Map<String, List<SearchHistoryEntry>> getEntriesFromHistoriesWithPrefix(
            String prefix, int maxEntriesPerHistory) {

        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }
        if (maxEntriesPerHistory < 0) {
            throw new IllegalArgumentException("Max entries per history cannot be negative");
        }

        Map<String, List<SearchHistoryEntry>> results = new HashMap<>();
        List<String> matchingNames = getHistoryNamesWithPrefix(prefix);

        for (String historyName : matchingNames) {
            try {
                SearchHistory history = new SearchHistory(historyName);
                List<SearchHistoryEntry> entries = maxEntriesPerHistory > 0
                        ? history.getSearchEntries(maxEntriesPerHistory)
                        : history.getSearchEntries();

                if (!entries.isEmpty()) {
                    results.put(historyName, entries);
                }
            } catch (Exception e) {
                // Log error but continue processing other histories
                java.util.logging.Logger.getLogger(SearchHistory.class.getName())
                        .warning("Failed to load history '" + historyName + "': " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * Retrieves all search entries from multiple histories that match the given
     * prefix,
     * with unlimited entries per history.
     * 
     * @param prefix the prefix to match against history names
     * @return a map of history names to their search entries (never null)
     * @throws IllegalArgumentException if prefix is null
     */
    public static Map<String, List<SearchHistoryEntry>> getEntriesFromHistoriesWithPrefix(String prefix) {
        return getEntriesFromHistoriesWithPrefix(prefix, 0);
    }

    /**
     * Deletes all search histories that match the given prefix.
     * This is useful for bulk cleanup operations.
     * 
     * @param prefix the prefix to match against history names
     * @return the number of histories that were deleted
     * @throws IllegalArgumentException if prefix is null
     */
    public static int deleteHistoriesWithPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }

        List<String> matchingNames = getHistoryNamesWithPrefix(prefix);
        int deletedCount = 0;

        try {
            Preferences prefs = PrefHelper.getUserPreferences();
            for (String historyName : matchingNames) {
                String prefKey = SearchHistory.class.getName() + "." + historyName;
                prefs.remove(prefKey);
                deletedCount++;
            }
        } catch (Exception e) {
            // Log error but return count of what was successfully deleted
            java.util.logging.Logger.getLogger(SearchHistory.class.getName())
                    .warning("Failed to delete some histories with prefix '" + prefix + "': " + e.getMessage());
        }

        return deletedCount;
    }

    /**
     * Checks if any search histories exist with the given prefix.
     * 
     * @param prefix the prefix to match against history names
     * @return true if at least one history exists with the given prefix
     * @throws IllegalArgumentException if prefix is null
     */
    public static boolean existsHistoryWithPrefix(String prefix) {
        return !getHistoryNamesWithPrefix(prefix).isEmpty();
    }

    /**
     * Gets the total number of search histories that match the given prefix.
     * 
     * @param prefix the prefix to match against history names
     * @return the count of matching histories
     * @throws IllegalArgumentException if prefix is null
     */
    public static int getHistoryCountWithPrefix(String prefix) {
        return getHistoryNamesWithPrefix(prefix).size();
    }

    /**
     * Loads the search entries from user preferences.
     * 
     * @return a mutable list of search entries
     */
    @SuppressWarnings("unchecked")
    private List<SearchHistoryEntry> loadSearchEntries() {
        try {
            ArrayList<SearchHistoryEntry> defaultList = new ArrayList<>();
            ArrayList<SearchHistoryEntry> entries = PrefHelper.getSerializedObject(prefKey, ArrayList.class,
                    defaultList);

            // Validate that all entries are SearchHistoryEntry instances
            List<SearchHistoryEntry> validEntries = new ArrayList<>();
            for (Object entry : entries) {
                if (entry instanceof SearchHistoryEntry) {
                    validEntries.add((SearchHistoryEntry) entry);
                }
            }

            return validEntries;
        } catch (Exception e) {
            // If there's any error loading, return empty list
            return new ArrayList<>();
        }
    }

    /**
     * Saves the search entries to user preferences.
     * 
     * @param entries the list of entries to save
     */
    private void saveSearchEntries(List<SearchHistoryEntry> entries) {
        try {
            Preferences prefs = PrefHelper.getUserPreferences();

            // Create a serializable ArrayList
            ArrayList<SearchHistoryEntry> serializableList = new ArrayList<>(entries);

            // Serialize and encode to Base64
            String encoded = Base64.encodeObject(serializableList);
            if (encoded != null) {
                // Store as byte array (PrefHelper.getSerializedObject expects this format)
                prefs.putByteArray(prefKey, encoded.getBytes());
            }
        } catch (Exception e) {
            // Log error but don't throw - preference saving should be non-fatal
            java.util.logging.Logger.getLogger(SearchHistory.class.getName())
                    .warning("Failed to save search history '" + historyName + "': " + e.getMessage());
        }
    }
}
