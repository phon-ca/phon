# Search History System Guide

## Overview

The Phon search history system provides a comprehensive way to manage and persist search queries across application sessions. It uses a prefix-based approach that allows different components to maintain separate search histories while sharing a common API.

## Architecture

### Design Pattern

The `SearchHistory` class follows a **static utility pattern** with prefix-based organization:

- **No instantiation required** - all methods are static
- **Prefix-based contexts** - different search contexts use unique prefixes
- **Thread-safe operations** - per-prefix locking ensures concurrent safety
- **Persistent storage** - uses Java Preferences API for cross-session persistence

### Key Components

```text
SearchHistory (Static Utility Class)
├── Static Methods (addSearchEntry, getSearchEntries, etc.)
├── Prefix-based Organization
├── Thread Safety (per-prefix locks)
└── Preferences Storage
```

## Basic Usage

### Adding Search Entries

```java
// Basic search entry
SearchHistory.addSearchEntry("query.phonex", entry);

// With custom max entries
SearchHistory.addSearchEntry("query.phonex", entry, 50);

// Simple convenience method
SearchHistoryEntry entry = SearchHistory.addSimpleSearchEntry(
    "query.phonex",
    "phoneme transcription",
    "phonex",
    false  // case sensitive
);
```

### Retrieving Search Entries

```java
// Get all entries for a prefix
List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries("query.phonex");

// Get limited number of recent entries
List<SearchHistoryEntry> recent = SearchHistory.getSearchEntries("query.phonex", 10);

// Get most recent entry
SearchHistoryEntry latest = SearchHistory.getMostRecentEntry("query.phonex");

// Check if history is empty
boolean isEmpty = SearchHistory.isEmpty("query.phonex");

// Get entry count
int count = SearchHistory.size("query.phonex");
```

### Managing History

```java
// Clear specific history
SearchHistory.clear("query.phonex");

// Delete specific entry
SearchHistory.deleteSearchEntry("query.phonex", entry);

// Delete by index
SearchHistoryEntry removed = SearchHistory.deleteSearchEntry("query.phonex", 0);

// Update existing entry
SearchHistory.updateSearchEntry("query.phonex", oldEntry, newEntry);
```

## Prefix Organization

### Recommended Prefix Patterns

Use hierarchical naming for logical organization:

```java
// Query-related searches
"query.phonex"           // PhonEx pattern searches
"query.regex"            // Regular expression searches
"query.participant"      // Participant name searches
"query.tier.ipa"         // IPA tier searches
"query.tier.orthography" // Orthography tier searches

// Analysis-related searches
"analysis.phoneme"       // Phoneme analysis queries
"analysis.syllable"      // Syllable structure queries
"analysis.segment"       // Segment-based queries

// Session editor contexts
"editor.search.main"     // Main search view
"editor.search.filter"   // Filter searches
"editor.find.replace"    // Find and replace

// Project-level searches
"project.corpus"         // Corpus-wide searches
"project.metadata"       // Metadata searches
```

### Context Separation Benefits

Different prefixes provide:

- **Logical separation** - phonex vs regex searches don't interfere
- **Specialized histories** - each search type maintains relevant entries
- **Performance** - smaller, focused history lists
- **User experience** - contextually relevant suggestions

## Advanced Features

### Finding Entries

```java
// Find by query text
List<SearchHistoryEntry> matches = SearchHistory.findEntriesByQueryText(
    "query.phonex", "phoneme pattern"
);

// Find by query type
List<SearchHistoryEntry> phonexEntries = SearchHistory.findEntriesByQueryType(
    "query", "phonex"
);

// Find by parameter
List<SearchHistoryEntry> targetEntries = SearchHistory.findEntriesByParameter(
    "query.phonex", "target"
);

// Find by parameter value
List<SearchHistoryEntry> ipaTargets = SearchHistory.findEntriesByParameterValue(
    "query.phonex", "target", "IPA Target"
);

// Check if entry exists
boolean exists = SearchHistory.containsEntry("query.phonex", entry);
```

### Bulk Operations

```java
// Get all history prefixes
List<String> allPrefixes = SearchHistory.getAllHistoryPrefixes();

// Find prefixes matching pattern
List<String> queryPrefixes = SearchHistory.getHistoryPrefixesWithPattern("query.");

// Bulk delete
int deleted = SearchHistory.deleteHistoriesWithPattern("temp.");

// Check existence
boolean hasQueryHistories = SearchHistory.existsHistoryWithPattern("query.");

// Count matching histories
int queryHistoryCount = SearchHistory.getHistoryCountWithPattern("query.");
```

## SearchHistoryEntry Structure

### Entry Components

```java
SearchHistoryEntry entry = SearchHistoryEntry.builder()
    .queryText("phoneme transcription")      // The search query
    .queryType("phonex")                     // Type of search
    .caseSensitive(false)                    // Case sensitivity
    .parameter("target", "IPA Target")       // Additional parameters
    .parameter("filter", "consonants")       // Multiple parameters supported
    .build();
```

### Entry Properties

- **Timestamp** - Automatic creation/modification time
- **Query Text** - The actual search string
- **Query Type** - Classification (phonex, regex, plain, etc.)
- **Case Sensitivity** - Boolean flag
- **Parameters** - Key-value pairs for additional context
- **Equality** - Based on content, not timestamp

## Integration Examples

### Session Editor Search View

```java
public class SearchView extends EditorView {
    private static final String SEARCH_PREFIX = "editor.search.main";

    private void onSuccessfulSearch() {
        String queryText = searchField.getText();
        String queryType = getSelectedSearchType(); // "phonex", "regex", "plain"
        boolean caseSensitive = caseSensitiveButton.isSelected();

        // Save to history
        SearchHistory.addSimpleSearchEntry(
            SEARCH_PREFIX, queryText, queryType, caseSensitive
        );
    }

    private void showSearchHistory() {
        List<SearchHistoryEntry> recent = SearchHistory.getSearchEntries(
            SEARCH_PREFIX, 20
        );

        // Display in popup or dropdown
        displayHistoryEntries(recent);
    }
}
```

### Participant Search Dialog

```java
public class ParticipantSearchDialog extends JDialog {
    private static final String PARTICIPANT_PREFIX = "dialog.participant.search";

    private void setupAutoComplete() {
        List<SearchHistoryEntry> history = SearchHistory.getSearchEntries(
            PARTICIPANT_PREFIX, 10
        );

        // Configure autocomplete with history
        autocompleteField.setCompletionItems(
            history.stream()
                   .map(SearchHistoryEntry::queryText)
                   .distinct()
                   .collect(Collectors.toList())
        );
    }
}
```

### Query Analysis Tool

```java
public class QueryAnalyzer {
    public void analyzePhonexUsage() {
        // Get all phonex queries across all contexts
        List<String> allPrefixes = SearchHistory.getAllHistoryPrefixes();

        for (String prefix : allPrefixes) {
            List<SearchHistoryEntry> phonexQueries =
                SearchHistory.findEntriesByQueryType(prefix, "phonex");

            analyzeQueryPatterns(prefix, phonexQueries);
        }
    }
}
```

## Best Practices

### Prefix Naming

1. **Use hierarchical structure**: `"category.subcategory.context"`
2. **Be descriptive**: `"query.phonex"` not `"q.p"`
3. **Stay consistent**: Use same naming patterns across the application
4. **Consider scope**: Session vs project vs application level

### Entry Management

1. **Set appropriate limits**: Use reasonable `maxEntries` values (10-50)
2. **Clean up temporary contexts**: Delete temp histories when done
3. **Preserve user intent**: Don't automatically modify user queries
4. **Handle errors gracefully**: Search history should never break main functionality

### Performance Considerations

1. **Use specific prefixes**: Avoid overly broad patterns in bulk operations
2. **Limit history size**: Large histories can impact UI responsiveness
3. **Background cleanup**: Periodically clean old or unused histories
4. **Batch operations**: Group multiple changes when possible

### User Experience

1. **Show recent first**: Most recent entries are usually most relevant
2. **Provide context**: Include query type and parameters in displays
3. **Allow manual management**: Let users clear or edit history
4. **Respect privacy**: Consider providing option to disable history

## Thread Safety

The system provides thread safety through:

- **Per-prefix locking**: Each prefix gets its own lock object using `ConcurrentHashMap`
- **Synchronized operations**: All storage operations are synchronized per prefix
- **Atomic updates**: Preference writes are atomic
- **Concurrent access**: Multiple prefixes can be accessed simultaneously without blocking

```java
// Safe to call from multiple threads
CompletableFuture.runAsync(() ->
    SearchHistory.addSimpleSearchEntry("thread1.query", "test1", "plain")
);

CompletableFuture.runAsync(() ->
    SearchHistory.addSimpleSearchEntry("thread2.query", "test2", "regex")
);
```

## Storage and Persistence

### Preference Keys

History entries are stored using keys like:

```text
ca.phon.util.SearchHistory.query.phonex
ca.phon.util.SearchHistory.editor.search.main
```

### Serialization

- **Format**: Uses `PrefHelper.getSerializedObject()` for loading and Base64-encoded serialized ArrayList for saving
- **Compatibility**: Handles version changes gracefully with validation
- **Recovery**: Falls back to empty history on corruption or loading errors
- **Validation**: Loading process validates that all entries are valid `SearchHistoryEntry` instances
- **Cross-platform**: Works on all Java-supported platforms

### Cleanup

```java
// Clean up all temporary histories
SearchHistory.deleteHistoriesWithPattern("temp.");

// Clean up old analysis histories
SearchHistory.deleteHistoriesWithPattern("analysis.old.");

// Complete cleanup (careful!)
SearchHistory.deleteHistoriesWithPattern("");
```

## Troubleshooting

### Common Issues

1. **History not persisting**

   - Check user preferences write permissions
   - Verify prefix format (no null/empty values)
   - Ensure proper application shutdown

2. **Performance problems**

   - Reduce history size limits
   - Use more specific prefixes
   - Clean up unused histories

3. **Memory usage**

   - Monitor large history accumulation
   - Implement periodic cleanup
   - Use appropriate maxEntries limits

4. **Loading/saving errors**
   - Non-fatal by design - operations continue with empty history
   - Check application logs for warning messages
   - Consider clearing corrupted history with `clear(prefix)`

### Debugging

```java
// Check if history exists
boolean exists = SearchHistory.existsHistoryWithPattern("your.prefix");

// Get current size
int size = SearchHistory.size("your.prefix");

// List all prefixes for debugging
List<String> allPrefixes = SearchHistory.getAllHistoryPrefixes();
System.out.println("Active history prefixes: " + allPrefixes);

// Check specific prefix patterns
List<String> queryPrefixes = SearchHistory.getHistoryPrefixesWithPattern("query.");
System.out.println("Query-related prefixes: " + queryPrefixes);
```

## Migration Guide

### From Instance-Based to Static API

If migrating from the old instance-based API:

```java
// Old way
SearchHistory queryHistory = new SearchHistory("query.phonex");
queryHistory.addSearchEntry(entry);
List<SearchHistoryEntry> entries = queryHistory.getSearchEntries();

// New way
SearchHistory.addSearchEntry("query.phonex", entry);
List<SearchHistoryEntry> entries = SearchHistory.getSearchEntries("query.phonex");
```

### Choosing Prefixes

When migrating, map old history names to appropriate prefixes:

- `"query.search"` → `"editor.search.main"`
- `"phonex.patterns"` → `"query.phonex"`
- `"participant.lookup"` → `"query.participant"`

## Future Enhancements

Potential areas for expansion:

1. **Search Analytics**: Track usage patterns and popular queries
2. **Cloud Sync**: Synchronize histories across devices
3. **Smart Suggestions**: ML-based query completion
4. **Export/Import**: Backup and restore functionality
5. **History Visualization**: Timeline and frequency analysis
6. **Collaborative Histories**: Shared team search patterns

---

For questions or suggestions regarding the search history system, please refer to the source code in `ca.phon.util.SearchHistory` or contact the development team.
