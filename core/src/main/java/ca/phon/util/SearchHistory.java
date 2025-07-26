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

import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * <p>
 * Generic search history manager that stores search entries in user
 * preferences.
 * This class provides methods for managing a list of search terms with support
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
 * SearchHistory&lt;String&gt; queryHistory = new SearchHistory&lt;&gt;("query.search", String.class);
 * queryHistory.addSearchEntry("phoneme transcription");
 * queryHistory.addSearchEntry("syllable structure");
 * List&lt;String&gt; recent = queryHistory.getSearchEntries();
 * </pre>
 */
public class SearchHistory<T extends Serializable> {

    /** Default maximum number of search entries to maintain */
    public static final int DEFAULT_MAX_ENTRIES = 20;

    private final String historyName;
    private final Class<T> entryType;
    private final int maxEntries;
    private final String prefKey;
    private final Object lock = new Object();

    /**
     * Creates a new search history with the default maximum number of entries.
     * 
     * @param historyName unique name for this search history
     * @param entryType   class type of search entries
     */
    public SearchHistory(String historyName, Class<T> entryType) {
        this(historyName, entryType, DEFAULT_MAX_ENTRIES);
    }

    /**
     * Creates a new search history with a specified maximum number of entries.
     * 
     * @param historyName unique name for this search history
     * @param entryType   class type of search entries
     * @param maxEntries  maximum number of entries to maintain (must be > 0)
     * @throws IllegalArgumentException if maxEntries <= 0
     */
    public SearchHistory(String historyName, Class<T> entryType, int maxEntries) {
        if (historyName == null || historyName.trim().isEmpty()) {
            throw new IllegalArgumentException("History name cannot be null or empty");
        }
        if (entryType == null) {
            throw new IllegalArgumentException("Entry type cannot be null");
        }
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("Max entries must be greater than 0");
        }

        this.historyName = historyName.trim();
        this.entryType = entryType;
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
    public void addSearchEntry(T entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Search entry cannot be null");
        }

        synchronized (lock) {
            List<T> entries = getSearchEntries();

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
    public void updateSearchEntry(T oldEntry, T newEntry) {
        if (newEntry == null) {
            throw new IllegalArgumentException("New search entry cannot be null");
        }

        synchronized (lock) {
            List<T> entries = getSearchEntries();

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
    public boolean deleteSearchEntry(T entry) {
        synchronized (lock) {
            List<T> entries = getSearchEntries();
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
    public T deleteSearchEntry(int index) {
        synchronized (lock) {
            List<T> entries = getSearchEntries();

            if (index < 0 || index >= entries.size()) {
                throw new IndexOutOfBoundsException("Index " + index + " out of range [0, " + entries.size() + ")");
            }

            T removed = entries.remove(index);
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
    public List<T> getSearchEntries() {
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
    public List<T> getSearchEntries(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }

        synchronized (lock) {
            List<T> allEntries = loadSearchEntries();
            int endIndex = Math.min(limit, allEntries.size());
            return new ArrayList<>(allEntries.subList(0, endIndex));
        }
    }

    /**
     * Gets the most recent search entry.
     * 
     * @return the most recent entry, or null if history is empty
     */
    public T getMostRecentEntry() {
        synchronized (lock) {
            List<T> entries = loadSearchEntries();
            return entries.isEmpty() ? null : entries.get(0);
        }
    }

    /**
     * Checks if the history contains the specified entry.
     * 
     * @param entry the entry to check for
     * @return true if the entry exists in the history
     */
    public boolean containsEntry(T entry) {
        synchronized (lock) {
            return loadSearchEntries().contains(entry);
        }
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
     * Loads the search entries from user preferences.
     * 
     * @return a mutable list of search entries
     */
    @SuppressWarnings("unchecked")
    private List<T> loadSearchEntries() {
        try {
            ArrayList<T> defaultList = new ArrayList<>();
            ArrayList<T> entries = PrefHelper.getSerializedObject(prefKey, ArrayList.class, defaultList);

            // Validate that all entries are of the correct type
            List<T> validEntries = new ArrayList<>();
            for (Object entry : entries) {
                if (entryType.isInstance(entry)) {
                    validEntries.add(entryType.cast(entry));
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
    private void saveSearchEntries(List<T> entries) {
        try {
            Preferences prefs = PrefHelper.getUserPreferences();

            // Create a serializable ArrayList
            ArrayList<T> serializableList = new ArrayList<>(entries);

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
