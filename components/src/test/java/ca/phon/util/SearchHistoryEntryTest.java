package ca.phon.util;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for SearchHistoryEntry
 */
public class SearchHistoryEntryTest {

        private LocalDateTime testDate;
        private String testQueryText;
        private String testQueryType;
        private Map<String, String> testParameters;

        @Before
        public void setUp() {
                testDate = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
                testQueryText = "test query";
                testQueryType = "phonex";
                testParameters = new HashMap<>();
                testParameters.put("target", "IPA Target");
                testParameters.put("group", "Word");
        }

        @Test
        public void testBuilderWithRequiredFields() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .build();

                assertEquals(testQueryText, entry.queryText());
                assertEquals(testQueryType, entry.queryType());
                assertFalse(entry.caseSensitive());
                assertTrue(entry.parameters().isEmpty());
                assertNotNull(entry.date());
        }

        @Test
        public void testBuilderWithAllFields() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .date(testDate)
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .caseSensitive(true)
                                .parameters(testParameters)
                                .build();

                assertEquals(testDate, entry.date());
                assertEquals(testQueryText, entry.queryText());
                assertEquals(testQueryType, entry.queryType());
                assertTrue(entry.caseSensitive());
                assertEquals(testParameters, entry.parameters());
        }

        @Test
        public void testBuilderWithIndividualParameters() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameter("key1", "value1")
                                .parameter("key2", "value2")
                                .build();

                assertEquals("value1", entry.getParameter("key1"));
                assertEquals("value2", entry.getParameter("key2"));
                assertTrue(entry.hasParameter("key1"));
                assertTrue(entry.hasParameter("key2"));
                assertFalse(entry.hasParameter("key3"));
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderNullQueryText() {
                SearchHistoryEntry.builder()
                                .queryText(null)
                                .queryType(testQueryType)
                                .build();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderEmptyQueryText() {
                SearchHistoryEntry.builder()
                                .queryText("   ")
                                .queryType(testQueryType)
                                .build();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderNullQueryType() {
                SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(null)
                                .build();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderEmptyQueryType() {
                SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType("   ")
                                .build();
        }

        @Test(expected = IllegalStateException.class)
        public void testBuilderMissingQueryText() {
                SearchHistoryEntry.builder()
                                .queryType(testQueryType)
                                .build();
        }

        @Test(expected = IllegalStateException.class)
        public void testBuilderMissingQueryType() {
                SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .build();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderNullParameterKey() {
                SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameter(null, "value")
                                .build();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderNullParameterValue() {
                SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameter("key", null)
                                .build();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderNullParametersMap() {
                SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameters(null)
                                .build();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderParametersMapWithNullKey() {
                Map<String, String> badParams = new HashMap<>();
                badParams.put(null, "value");

                SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameters(badParams)
                                .build();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testBuilderParametersMapWithNullValue() {
                Map<String, String> badParams = new HashMap<>();
                badParams.put("key", null);

                SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameters(badParams)
                                .build();
        }

        @Test
        public void testBuilderRemoveParameter() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameter("key1", "value1")
                                .parameter("key2", "value2")
                                .removeParameter("key1")
                                .build();

                assertFalse(entry.hasParameter("key1"));
                assertTrue(entry.hasParameter("key2"));
                assertEquals("value2", entry.getParameter("key2"));
        }

        @Test
        public void testBuilderClearParameters() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameter("key1", "value1")
                                .parameter("key2", "value2")
                                .clearParameters()
                                .build();

                assertTrue(entry.parameters().isEmpty());
        }

        @Test
        public void testToBuilder() {
                SearchHistoryEntry original = SearchHistoryEntry.builder()
                                .date(testDate)
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .caseSensitive(true)
                                .parameters(testParameters)
                                .build();

                SearchHistoryEntry copy = original.toBuilder()
                                .queryText("modified query")
                                .build();

                assertEquals(testDate, copy.date());
                assertEquals("modified query", copy.queryText());
                assertEquals(testQueryType, copy.queryType());
                assertTrue(copy.caseSensitive());
                assertEquals(testParameters, copy.parameters());
        }

        @Test
        public void testGetParameterNotFound() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .build();

                assertNull(entry.getParameter("nonexistent"));
        }

        @Test
        public void testGetParametersImmutable() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .parameter("key1", "value1")
                                .build();

                Map<String, String> params = entry.parameters();

                // Record returns immutable map, so this should throw exception
                try {
                        params.put("key2", "value2");
                        fail("Expected UnsupportedOperationException");
                } catch (UnsupportedOperationException e) {
                        // Expected - parameters() returns immutable map
                }

                // Original entry should not be affected
                assertFalse(entry.hasParameter("key2"));
        }

        @Test
        public void testEquals() {
                SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                                .date(testDate)
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .caseSensitive(true)
                                .parameters(testParameters)
                                .build();

                SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                                .date(testDate)
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .caseSensitive(true)
                                .parameters(testParameters)
                                .build();

                assertEquals(entry1, entry2);
                assertEquals(entry1.hashCode(), entry2.hashCode());
        }

        @Test
        public void testNotEquals() {
                SearchHistoryEntry entry1 = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .build();

                SearchHistoryEntry entry2 = SearchHistoryEntry.builder()
                                .queryText("different query")
                                .queryType(testQueryType)
                                .build();

                assertNotEquals(entry1, entry2);
        }

        @Test
        public void testEqualsWithNull() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .build();

                assertNotEquals(entry, null);
        }

        @Test
        public void testEqualsWithDifferentClass() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .build();

                assertNotEquals(entry, "string");
        }

        @Test
        public void testToString() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .date(testDate)
                                .queryText(testQueryText)
                                .queryType(testQueryType)
                                .caseSensitive(true)
                                .parameter("key1", "value1")
                                .build();

                String str = entry.toString();
                assertTrue(str.contains("SearchHistoryEntry"));
                assertTrue(str.contains(testQueryText));
                assertTrue(str.contains(testQueryType));
                assertTrue(str.contains("true"));
                assertTrue(str.contains("key1"));
        }

        @Test
        public void testTrimWhitespace() {
                SearchHistoryEntry entry = SearchHistoryEntry.builder()
                                .queryText("  " + testQueryText + "  ")
                                .queryType("  " + testQueryType + "  ")
                                .build();

                assertEquals(testQueryText, entry.queryText());
                assertEquals(testQueryType, entry.queryType());
        }
}
