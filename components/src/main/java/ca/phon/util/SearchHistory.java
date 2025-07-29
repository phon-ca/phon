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
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

/**
 * <p>
 * Search history manager that stores comprehensive search entries in user
 * preferences using context-based prefixes.
 * This class provides static methods for managing search history entries
 * with support for adding, updating, deleting, and retrieving search history
 * entries
 * across different contexts using prefix-based organization.
 * </p>
 * 
 * <p>
 * The search history is stored using the Java Preferences API and persists
 * across application sessions. Different search contexts can maintain separate
 * histories by using unique prefixes (e.g., "query.phonex",
 * "search.participant").
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
 * <li>Static utility methods with context prefixes for multiple independent
 * histories</li>
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
 * String prefix = "query.phonex";
 * SearchHistoryEntry entry = SearchHistoryEntry.builder()
 *         .queryText("phoneme transcription")
 *         .queryType("phonex")
 *         .caseSensitive(false)
 *         .parameter("target", "IPA Target")
 *         .build();
 * SearchHistory.addSearchEntry(prefix, entry);
 * List&lt;SearchHistoryEntry&gt; recent = SearchHistory.getSearchEntries(prefix);
 * </pre>
 */
public final class SearchHistory {

    /** Default maximum number of search entries to maintain */
    public static final int DEFAULT_MAX_ENTRIES = 20;

    /** Cache for locks per prefix to ensure thread safety */
    private static final Map<String, Object> prefixLocks = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private SearchHistory() {
        throw new UnsupportedOperationException("SearchHistory is a utility class and cannot be instantiated");
    }

    /**
     * Gets or creates a lock object for the given prefix to ensure thread safety.
     * 
     * @param prefix the prefix to get a lock for
     * @return a lock object for the prefix
     */
    private static Object getLockForPrefix(String prefix) {
        return prefixLocks.computeIfAbsent(prefix, k -> new Object());
    }

    /**
     * Creates a preference key for the given prefix.
     * 
     * @param prefix the prefix for the search history
     * @return the preference key
     */
    private static String createPrefKey(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }
        return SearchHistory.class.getName() + "." + prefix.trim();
    }

    /**
     * Checks if two search history entries have the same query text and parameters.
     * This is used to determine if entries are duplicates for deduplication
     * purposes.
     * 
     * @param entry1 the first entry to compare
     * @param entry2 the second entry to compare
     * @return true if both entries have the same query text and parameters
     */
    private static boolean entriesMatch(SearchHistoryEntry entry1, SearchHistoryEntry entry2) {
        return entry1.queryText().equals(entry2.queryText()) &&
                entry1.parameters().equals(entry2.parameters());
    }

    /**
     * Adds a new search entry to the history for the given prefix. If an entry with
     * the same query text and parameters already exists, it is removed before
     * adding
     * the new entry to the front of the list. If the history exceeds the maximum
     * size,
     * the oldest entries are removed.
     * 
     * @param prefix the prefix for the search history context
     * @param entry  the search entry to add (cannot be null)
     * @throws IllegalArgumentException if prefix or entry is null
     */
    public static void addSearchEntry(String prefix, SearchHistoryEntry entry) {
        addSearchEntry(prefix, entry, DEFAULT_MAX_ENTRIES);
    }

    /**
     * Adds a new search entry to the history for the given prefix with a specific
     * maximum size.
     * If an entry with the same query text and parameters already exists, it is
     * removed
     * before adding the new entry to the front of the list. If the history exceeds
     * the
     * maximum size, the oldest entries are removed.
     * 
     * @param prefix     the prefix for the search history context
     * @param entry      the search entry to add (cannot be null)
     * @param maxEntries maximum number of entries to maintain (must be > 0)
     * @throws IllegalArgumentException if prefix or entry is null, or maxEntries <=
     *                                  0
     */
    public static void addSearchEntry(String prefix, SearchHistoryEntry entry, int maxEntries) {
        if (entry == null) {
            throw new IllegalArgumentException("Search entry cannot be null");
        }
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("Max entries must be greater than 0");
        }

        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> entries = loadSearchEntries(prefKey);

            // Remove any existing entries with same query text and parameters
            entries.removeIf(existingEntry -> entriesMatch(existingEntry, entry));

            // Add to front
            entries.add(0, entry);

            // Trim to max size
            while (entries.size() > maxEntries) {
                entries.remove(entries.size() - 1);
            }

            saveSearchEntries(prefKey, entries);
        }
    }

    /**
     * Updates an existing search entry in the history for the given prefix. If the
     * old entry exists,
     * it is replaced with the new entry at the same position. If the old entry
     * doesn't exist,
     * the new entry is added to the front.
     * 
     * @param prefix   the prefix for the search history context
     * @param oldEntry the entry to replace
     * @param newEntry the replacement entry (cannot be null)
     * @throws IllegalArgumentException if prefix or newEntry is null
     */
    public static void updateSearchEntry(String prefix, SearchHistoryEntry oldEntry, SearchHistoryEntry newEntry) {
        updateSearchEntry(prefix, oldEntry, newEntry, DEFAULT_MAX_ENTRIES);
    }

    /**
     * Updates an existing search entry in the history for the given prefix with a
     * specific maximum size.
     * If the old entry exists, it is replaced with the new entry at the same
     * position. If the old entry doesn't exist,
     * the new entry is added to the front.
     * 
     * @param prefix     the prefix for the search history context
     * @param oldEntry   the entry to replace
     * @param newEntry   the replacement entry (cannot be null)
     * @param maxEntries maximum number of entries to maintain (must be > 0)
     * @throws IllegalArgumentException if prefix or newEntry is null, or maxEntries
     *                                  <= 0
     */
    public static void updateSearchEntry(String prefix, SearchHistoryEntry oldEntry, SearchHistoryEntry newEntry,
            int maxEntries) {
        if (newEntry == null) {
            throw new IllegalArgumentException("New search entry cannot be null");
        }
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("Max entries must be greater than 0");
        }

        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> entries = loadSearchEntries(prefKey);

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

            saveSearchEntries(prefKey, entries);
        }
    }

    /**
     * Removes a search entry from the history for the given prefix.
     * 
     * @param prefix the prefix for the search history context
     * @param entry  the entry to remove
     * @return true if the entry was found and removed, false otherwise
     * @throws IllegalArgumentException if prefix is null
     */
    public static boolean deleteSearchEntry(String prefix, SearchHistoryEntry entry) {
        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> entries = loadSearchEntries(prefKey);
            boolean removed = entries.remove(entry);

            if (removed) {
                saveSearchEntries(prefKey, entries);
            }

            return removed;
        }
    }

    /**
     * Removes a search entry at the specified index for the given prefix.
     * 
     * @param prefix the prefix for the search history context
     * @param index  the index of the entry to remove
     * @return the removed entry
     * @throws IllegalArgumentException  if prefix is null
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public static SearchHistoryEntry deleteSearchEntry(String prefix, int index) {
        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> entries = loadSearchEntries(prefKey);

            if (index < 0 || index >= entries.size()) {
                throw new IndexOutOfBoundsException("Index " + index + " out of range [0, " + entries.size() + ")");
            }

            SearchHistoryEntry removed = entries.remove(index);
            saveSearchEntries(prefKey, entries);

            return removed;
        }
    }

    /**
     * Retrieves all search entries for the given prefix in most-recently-used
     * order.
     * The returned list is a copy and modifications will not affect the stored
     * history.
     * 
     * @param prefix the prefix for the search history context
     * @return a new list containing all search entries (never null)
     * @throws IllegalArgumentException if prefix is null
     */
    public static List<SearchHistoryEntry> getSearchEntries(String prefix) {
        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            return loadSearchEntries(prefKey);
        }
    }

    /**
     * Retrieves up to the specified number of most recent search entries for the
     * given prefix.
     * 
     * @param prefix the prefix for the search history context
     * @param limit  maximum number of entries to return
     * @return a new list containing the most recent entries (never null)
     * @throws IllegalArgumentException if prefix is null or limit < 0
     */
    public static List<SearchHistoryEntry> getSearchEntries(String prefix, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }

        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries(prefKey);
            int endIndex = Math.min(limit, allEntries.size());
            return new ArrayList<>(allEntries.subList(0, endIndex));
        }
    }

    /**
     * Gets the most recent search entry for the given prefix.
     * 
     * @param prefix the prefix for the search history context
     * @return the most recent entry, or null if history is empty
     * @throws IllegalArgumentException if prefix is null
     */
    public static SearchHistoryEntry getMostRecentEntry(String prefix) {
        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> entries = loadSearchEntries(prefKey);
            return entries.isEmpty() ? null : entries.get(0);
        }
    }

    /**
     * Checks if the history for the given prefix contains the specified entry.
     * 
     * @param prefix the prefix for the search history context
     * @param entry  the entry to check for
     * @return true if the entry exists in the history
     * @throws IllegalArgumentException if prefix is null
     */
    public static boolean containsEntry(String prefix, SearchHistoryEntry entry) {
        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            return loadSearchEntries(prefKey).contains(entry);
        }
    }

    /**
     * Finds entries that match the given query text for the given prefix.
     * 
     * @param prefix    the prefix for the search history context
     * @param queryText the query text to search for
     * @return a list of matching entries (never null)
     * @throws IllegalArgumentException if prefix is null
     */
    public static List<SearchHistoryEntry> findEntriesByQueryText(String prefix, String queryText) {
        if (queryText == null) {
            return new ArrayList<>();
        }

        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries(prefKey);
            List<SearchHistoryEntry> matches = new ArrayList<>();

            for (SearchHistoryEntry entry : allEntries) {
                if (queryText.equals(entry.queryText())) {
                    matches.add(entry);
                }
            }

            return matches;
        }
    }

    /**
     * Finds entries that match the given query type for the given prefix.
     * 
     * @param prefix    the prefix for the search history context
     * @param queryType the query type to search for
     * @return a list of matching entries (never null)
     * @throws IllegalArgumentException if prefix is null
     */
    public static List<SearchHistoryEntry> findEntriesByQueryType(String prefix, String queryType) {
        if (queryType == null) {
            return new ArrayList<>();
        }

        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries(prefKey);
            List<SearchHistoryEntry> matches = new ArrayList<>();

            for (SearchHistoryEntry entry : allEntries) {
                if (queryType.equals(entry.queryType())) {
                    matches.add(entry);
                }
            }

            return matches;
        }
    }

    /**
     * Finds entries that contain the specified parameter for the given prefix.
     * 
     * @param prefix       the prefix for the search history context
     * @param parameterKey the parameter key to search for
     * @return a list of matching entries (never null)
     * @throws IllegalArgumentException if prefix is null
     */
    public static List<SearchHistoryEntry> findEntriesByParameter(String prefix, String parameterKey) {
        if (parameterKey == null) {
            return new ArrayList<>();
        }

        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries(prefKey);
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
     * Finds entries that have a specific parameter value for the given prefix.
     * 
     * @param prefix         the prefix for the search history context
     * @param parameterKey   the parameter key
     * @param parameterValue the parameter value to search for
     * @return a list of matching entries (never null)
     * @throws IllegalArgumentException if prefix is null
     */
    public static List<SearchHistoryEntry> findEntriesByParameterValue(String prefix, String parameterKey,
            String parameterValue) {
        if (parameterKey == null || parameterValue == null) {
            return new ArrayList<>();
        }

        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            List<SearchHistoryEntry> allEntries = loadSearchEntries(prefKey);
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
     * Adds a simple search entry with just query text and type for the given
     * prefix.
     * This is a convenience method for basic search entries.
     * 
     * @param prefix    the prefix for the search history context
     * @param queryText the query text
     * @param queryType the query type
     * @return the created entry that was added
     * @throws IllegalArgumentException if prefix, queryText or queryType is null or
     *                                  empty
     */
    public static SearchHistoryEntry addSimpleSearchEntry(String prefix, String queryText, String queryType) {
        return addSimpleSearchEntry(prefix, queryText, queryType, false);
    }

    /**
     * Adds a simple search entry with query text, type, and case sensitivity for
     * the given prefix.
     * This is a convenience method for basic search entries.
     * 
     * @param prefix        the prefix for the search history context
     * @param queryText     the query text
     * @param queryType     the query type
     * @param caseSensitive whether the search is case sensitive
     * @return the created entry that was added
     * @throws IllegalArgumentException if prefix, queryText or queryType is null or
     *                                  empty
     */
    public static SearchHistoryEntry addSimpleSearchEntry(String prefix, String queryText, String queryType,
            boolean caseSensitive) {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText(queryText)
                .queryType(queryType)
                .caseSensitive(caseSensitive)
                .build();

        addSearchEntry(prefix, entry);
        return entry;
    }

    /**
     * Gets the current number of entries in the history for the given prefix.
     * 
     * @param prefix the prefix for the search history context
     * @return the number of entries
     * @throws IllegalArgumentException if prefix is null
     */
    public static int size(String prefix) {
        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            return loadSearchEntries(prefKey).size();
        }
    }

    /**
     * Checks if the history for the given prefix is empty.
     * 
     * @param prefix the prefix for the search history context
     * @return true if the history contains no entries
     * @throws IllegalArgumentException if prefix is null
     */
    public static boolean isEmpty(String prefix) {
        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            return loadSearchEntries(prefKey).isEmpty();
        }
    }

    /**
     * Removes all entries from the search history for the given prefix.
     * 
     * @param prefix the prefix for the search history context
     * @throws IllegalArgumentException if prefix is null
     */
    public static void clear(String prefix) {
        String prefKey = createPrefKey(prefix);
        Object lock = getLockForPrefix(prefix);

        synchronized (lock) {
            Preferences prefs = PrefHelper.getUserPreferences();
            prefs.remove(prefKey);
        }
    }

    /**
     * Retrieves all search history prefixes that match the given prefix pattern.
     * This is useful for finding related search histories or implementing
     * wildcard-based searches.
     * 
     * @param prefixPattern the prefix pattern to match against history prefixes
     * @return a list of history prefixes that start with the given pattern (never
     *         null)
     * @throws IllegalArgumentException if prefixPattern is null
     */
    public static List<String> getHistoryPrefixesWithPattern(String prefixPattern) {
        if (prefixPattern == null) {
            throw new IllegalArgumentException("Prefix pattern cannot be null");
        }

        List<String> matchingPrefixes = new ArrayList<>();
        try {
            Preferences prefs = PrefHelper.getUserPreferences();
            String searchPrefix = SearchHistory.class.getName() + "." + prefixPattern;

            String[] keys = prefs.keys();
            for (String key : keys) {
                if (key.startsWith(searchPrefix)) {
                    // Extract the history prefix from the full preference key
                    String historyPrefix = key.substring(SearchHistory.class.getName().length() + 1);
                    matchingPrefixes.add(historyPrefix);
                }
            }
        } catch (Exception e) {
            // Log error but return empty list - preference reading should be non-fatal
            java.util.logging.Logger.getLogger(SearchHistory.class.getName())
                    .warning("Failed to retrieve history prefixes with pattern '" + prefixPattern + "': "
                            + e.getMessage());
        }

        return matchingPrefixes;
    }

    /**
     * Retrieves all search history prefixes stored in user preferences.
     * This returns all SearchHistory prefixes that have been created and have data.
     * 
     * @return a list of all history prefixes (never null)
     */
    public static List<String> getAllHistoryPrefixes() {
        return getHistoryPrefixesWithPattern("");
    }

    /**
     * Deletes all search histories that match the given prefix pattern.
     * This is useful for bulk cleanup operations.
     * 
     * @param prefixPattern the prefix pattern to match against history prefixes
     * @return the number of histories that were deleted
     * @throws IllegalArgumentException if prefixPattern is null
     */
    public static int deleteHistoriesWithPattern(String prefixPattern) {
        if (prefixPattern == null) {
            throw new IllegalArgumentException("Prefix pattern cannot be null");
        }

        List<String> matchingPrefixes = getHistoryPrefixesWithPattern(prefixPattern);
        int deletedCount = 0;

        try {
            Preferences prefs = PrefHelper.getUserPreferences();
            for (String historyPrefix : matchingPrefixes) {
                String prefKey = SearchHistory.class.getName() + "." + historyPrefix;
                prefs.remove(prefKey);
                deletedCount++;
            }
        } catch (Exception e) {
            // Log error but return count of what was successfully deleted
            java.util.logging.Logger.getLogger(SearchHistory.class.getName())
                    .warning("Failed to delete some histories with pattern '" + prefixPattern + "': " + e.getMessage());
        }

        return deletedCount;
    }

    /**
     * Checks if any search histories exist with the given prefix pattern.
     * 
     * @param prefixPattern the prefix pattern to match against history prefixes
     * @return true if at least one history exists with the given pattern
     * @throws IllegalArgumentException if prefixPattern is null
     */
    public static boolean existsHistoryWithPattern(String prefixPattern) {
        return !getHistoryPrefixesWithPattern(prefixPattern).isEmpty();
    }

    /**
     * Gets the total number of search histories that match the given prefix
     * pattern.
     * 
     * @param prefixPattern the prefix pattern to match against history prefixes
     * @return the count of matching histories
     * @throws IllegalArgumentException if prefixPattern is null
     */
    public static int getHistoryCountWithPattern(String prefixPattern) {
        return getHistoryPrefixesWithPattern(prefixPattern).size();
    }

    /**
     * Loads the search entries from user preferences for the given preference key.
     * 
     * @param prefKey the preference key to load from
     * @return a mutable list of search entries
     */
    @SuppressWarnings("unchecked")
    private static List<SearchHistoryEntry> loadSearchEntries(String prefKey) {
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
     * Saves the search entries to user preferences for the given preference key.
     * 
     * @param prefKey the preference key to save to
     * @param entries the list of entries to save
     */
    private static void saveSearchEntries(String prefKey, List<SearchHistoryEntry> entries) {
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
                    .warning("Failed to save search history: " + e.getMessage());
        }
    }
}
