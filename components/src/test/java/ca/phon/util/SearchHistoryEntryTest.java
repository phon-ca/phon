package ca.phon.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SearchHistoryEntry}.
 * Tests cover builder pattern, validation, immutability, and serialization.
 */
@RunWith(JUnit4.class)
public class SearchHistoryEntryTest {

    @Test
    public void testBasicBuilder() {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test query")
                .queryType(SearchType.PHONEX)
                .build();

        assertEquals("test query", entry.queryText());
        assertEquals(SearchType.PHONEX, entry.queryType());
        assertFalse(entry.caseSensitive());
        assertTrue(entry.parameters().isEmpty());
        assertNotNull(entry.date());
    }

    @Test
    public void testBuilderWithAllFields() {
        LocalDateTime testDate = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
        Map<String, String> params = new HashMap<>();
        params.put("target", "IPA Target");
        params.put("group", "Word");

        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .date(testDate)
                .queryText("phoneme pattern")
                .queryType(SearchType.PHONEX)
                .caseSensitive(true)
                .parameters(params)
                .build();

        assertEquals(testDate, entry.date());
        assertEquals("phoneme pattern", entry.queryText());
        assertEquals(SearchType.PHONEX, entry.queryType());
        assertTrue(entry.caseSensitive());
        assertEquals(2, entry.parameters().size());
        assertEquals("IPA Target", entry.getParameter("target"));
        assertEquals("Word", entry.getParameter("group"));
    }

    @Test
    public void testBuilderWithIndividualParameters() {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test")
                .queryType(SearchType.REGEX)
                .parameter("key1", "value1")
                .parameter("key2", "value2")
                .build();

        assertEquals(2, entry.parameters().size());
        assertEquals("value1", entry.getParameter("key1"));
        assertEquals("value2", entry.getParameter("key2"));
    }

    @Test
    public void testParameterManagement() {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test")
                .queryType(SearchType.PLAIN)
                .parameter("keep", "this")
                .parameter("remove", "this")
                .removeParameter("remove")
                .build();

        assertEquals(1, entry.parameters().size());
        assertEquals("this", entry.getParameter("keep"));
        assertNull(entry.getParameter("remove"));
        assertFalse(entry.hasParameter("remove"));
        assertTrue(entry.hasParameter("keep"));
    }

    @Test
    public void testClearParameters() {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test")
                .queryType(SearchType.PLAIN)
                .parameter("key1", "value1")
                .parameter("key2", "value2")
                .clearParameters()
                .parameter("key3", "value3")
                .build();

        assertEquals(1, entry.parameters().size());
        assertEquals("value3", entry.getParameter("key3"));
        assertNull(entry.getParameter("key1"));
        assertNull(entry.getParameter("key2"));
    }

    @Test
    public void testToBuilder() {
        SearchHistoryEntry original = SearchHistoryEntry.builder()
                .queryText("original query")
                .queryType(SearchType.PHONEX)
                .caseSensitive(true)
                .parameter("param1", "value1")
                .build();

        SearchHistoryEntry modified = original.toBuilder()
                .queryText("modified query")
                .parameter("param2", "value2")
                .build();

        // Original should be unchanged
        assertEquals("original query", original.queryText());
        assertEquals(1, original.parameters().size());

        // Modified should have changes
        assertEquals("modified query", modified.queryText());
        assertEquals(SearchType.PHONEX, modified.queryType()); // Inherited
        assertTrue(modified.caseSensitive()); // Inherited
        assertEquals(2, modified.parameters().size()); // Added to
        assertEquals("value1", modified.getParameter("param1"));
        assertEquals("value2", modified.getParameter("param2"));
    }

    @Test
    public void testImmutability() {
        Map<String, String> mutableParams = new HashMap<>();
        mutableParams.put("key", "value");

        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test")
                .queryType(SearchType.PLAIN)
                .parameters(mutableParams)
                .build();

        // Modify original map
        mutableParams.put("key2", "value2");
        mutableParams.put("key", "modified");

        // Entry should not be affected
        assertEquals(1, entry.parameters().size());
        assertEquals("value", entry.getParameter("key"));
        assertNull(entry.getParameter("key2"));

        // Try to modify returned parameters map
        try {
            entry.parameters().put("new", "value");
            fail("Should not be able to modify parameters map");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testEquality() {
        LocalDateTime date = LocalDateTime.now();

        SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                .date(date)
                .queryText("test")
                .queryType(SearchType.PHONEX)
                .caseSensitive(true)
                .parameter("key", "value")
                .build();

        SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                .date(date)
                .queryText("test")
                .queryType(SearchType.PHONEX)
                .caseSensitive(true)
                .parameter("key", "value")
                .build();

        SearchHistoryEntry entry3 = SearchHistoryEntry.builder()
                .date(date)
                .queryText("different")
                .queryType(SearchType.PHONEX)
                .caseSensitive(true)
                .parameter("key", "value")
                .build();

        assertEquals(entry1, entry2);
        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertNotEquals(entry1, entry3);
        assertNotEquals(entry1.hashCode(), entry3.hashCode());
    }

    @Test
    public void testValidation() {
        // Test null query text
        try {
            SearchHistoryEntry.builder()
                    .queryText(null)
                    .queryType(SearchType.PHONEX)
                    .build();
            fail("Should throw exception for null query text");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Query text cannot be null"));
        }

        // Test empty query text
        try {
            SearchHistoryEntry.builder()
                    .queryText("  ")
                    .queryType(SearchType.PHONEX)
                    .build();
            fail("Should throw exception for empty query text");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Query text cannot be null"));
        }

        // Test null query type
        try {
            SearchHistoryEntry.builder()
                    .queryText("test")
                    .queryType(null)
                    .build();
            fail("Should throw exception for null query type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Query type cannot be null"));
        }

        // Test null parameter key
        try {
            SearchHistoryEntry.builder()
                    .queryText("test")
                    .queryType(SearchType.PHONEX)
                    .parameter(null, "value")
                    .build();
            fail("Should throw exception for null parameter key");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Parameter key cannot be null"));
        }

        // Test null parameter value
        try {
            SearchHistoryEntry.builder()
                    .queryText("test")
                    .queryType(SearchType.PHONEX)
                    .parameter("key", null)
                    .build();
            fail("Should throw exception for null parameter value");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Parameter value cannot be null"));
        }

        // Test missing required fields
        try {
            SearchHistoryEntry.builder()
                    .queryType(SearchType.PHONEX)
                    .build();
            fail("Should throw exception for missing query text");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Query text is required"));
        }
    }

    @Test
    public void testTextTrimming() {
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("  trimmed query  ")
                .queryType(SearchType.PHONEX)
                .build();

        assertEquals("trimmed query", entry.queryText());
        assertEquals(SearchType.PHONEX, entry.queryType());
    }

    @Test
    public void testDefaultDate() {
        LocalDateTime before = LocalDateTime.now();

        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("test")
                .queryType(SearchType.PHONEX)
                .build();

        LocalDateTime after = LocalDateTime.now();

        assertNotNull(entry.date());
        assertTrue("Date should be after or equal to before time",
                !entry.date().isBefore(before));
        assertTrue("Date should be before or equal to after time",
                !entry.date().isAfter(after));
    }

    @Test
    public void testBuilderParametersValidation() {
        Map<String, String> invalidParams = new HashMap<>();
        invalidParams.put("valid", "value");
        invalidParams.put(null, "invalid");

        try {
            SearchHistoryEntry.builder()
                    .queryText("test")
                    .queryType(SearchType.PHONEX)
                    .parameters(invalidParams)
                    .build();
            fail("Should throw exception for null parameter key in map");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Parameter key cannot be null"));
        }

        invalidParams.clear();
        invalidParams.put("key", null);

        try {
            SearchHistoryEntry.builder()
                    .queryText("test")
                    .queryType(SearchType.PHONEX)
                    .parameters(invalidParams)
                    .build();
            fail("Should throw exception for null parameter value in map");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Parameter value cannot be null"));
        }
    }

    @Test
    public void testSerialization() {
        // Test that the class is Serializable
        SearchHistoryEntry entry = SearchHistoryEntry.builder()
                .queryText("serialization test")
                .queryType(SearchType.PHONEX)
                .caseSensitive(true)
                .parameter("test", "value")
                .build();

        assertTrue("SearchHistoryEntry should be Serializable",
                java.io.Serializable.class.isAssignableFrom(SearchHistoryEntry.class));

        // Ensure entry was properly created
        assertNotNull("Entry should not be null", entry);
        assertEquals("serialization test", entry.queryText());
    }
}
