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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Represents a single search history entry with comprehensive metadata.
 * Each entry contains the search query, type, search options, and timestamp
 * information needed for advanced search history management.
 * </p>
 * 
 * <p>
 * This record is immutable to ensure thread safety and data integrity.
 * Use the {@link Builder} to create new instances or modify existing ones.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * SearchHistoryEntry entry = SearchHistoryEntry.builder()
 *         .queryText("phoneme transcription")
 *         .queryType("phonex")
 *         .caseSensitive(false)
 *         .parameter("target", "IPA Target")
 *         .parameter("group", "Word")
 *         .build();
 * </pre>
 */
public record SearchHistoryEntry(
        LocalDateTime date,
        String queryText,
        String queryType,
        boolean caseSensitive,
        Map<String, String> parameters) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Compact constructor with validation and defensive copying.
     */
    public SearchHistoryEntry {
        if (queryText == null || queryText.trim().isEmpty()) {
            throw new IllegalArgumentException("Query text cannot be null or empty");
        }
        if (queryType == null || queryType.trim().isEmpty()) {
            throw new IllegalArgumentException("Query type cannot be null or empty");
        }

        // Use current time if date is null
        date = date != null ? date : LocalDateTime.now();

        // Trim and validate required fields
        queryText = queryText.trim();
        queryType = queryType.trim();

        // Defensive copy of parameters map
        if (parameters == null) {
            parameters = Map.of();
        } else {
            // Validate no null keys or values
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException("Parameter key cannot be null");
                }
                if (entry.getValue() == null) {
                    throw new IllegalArgumentException("Parameter value cannot be null");
                }
            }
            parameters = Map.copyOf(parameters);
        }
    }

    /**
     * Gets the value of a specific parameter.
     * 
     * @param key the parameter key
     * @return the parameter value, or null if not found
     */
    public String getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * Checks if a parameter exists.
     * 
     * @param key the parameter key
     * @return true if the parameter exists
     */
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    /**
     * Creates a new builder instance.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder initialized with this entry's values.
     * 
     * @return a new builder with this entry's values
     */
    public Builder toBuilder() {
        return new Builder()
                .date(this.date())
                .queryText(this.queryText())
                .queryType(this.queryType())
                .caseSensitive(this.caseSensitive())
                .parameters(this.parameters());
    }

    /**
     * Builder class for creating {@link SearchHistoryEntry} instances.
     * This builder follows the fluent interface pattern and provides validation
     * of required fields.
     */
    public static final class Builder {
        private LocalDateTime date;
        private String queryText;
        private String queryType;
        private boolean caseSensitive;
        private Map<String, String> parameters = new HashMap<>();

        private Builder() {
        }

        /**
         * Sets the search date. If not specified, the current date/time will be used.
         * 
         * @param date the search date
         * @return this builder
         */
        public Builder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        /**
         * Sets the query text.
         * 
         * @param queryText the query text (required, cannot be null or empty)
         * @return this builder
         * @throws IllegalArgumentException if queryText is null or empty
         */
        public Builder queryText(String queryText) {
            if (queryText == null || queryText.trim().isEmpty()) {
                throw new IllegalArgumentException("Query text cannot be null or empty");
            }
            this.queryText = queryText.trim();
            return this;
        }

        /**
         * Sets the query type.
         * 
         * @param queryType the query type (required, cannot be null or empty)
         * @return this builder
         * @throws IllegalArgumentException if queryType is null or empty
         */
        public Builder queryType(String queryType) {
            if (queryType == null || queryType.trim().isEmpty()) {
                throw new IllegalArgumentException("Query type cannot be null or empty");
            }
            this.queryType = queryType.trim();
            return this;
        }

        /**
         * Sets whether the search is case sensitive.
         * 
         * @param caseSensitive true for case sensitive search
         * @return this builder
         */
        public Builder caseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        /**
         * Adds a single parameter.
         * 
         * @param key   the parameter key (cannot be null)
         * @param value the parameter value (cannot be null)
         * @return this builder
         * @throws IllegalArgumentException if key or value is null
         */
        public Builder parameter(String key, String value) {
            if (key == null) {
                throw new IllegalArgumentException("Parameter key cannot be null");
            }
            if (value == null) {
                throw new IllegalArgumentException("Parameter value cannot be null");
            }
            this.parameters.put(key, value);
            return this;
        }

        /**
         * Adds multiple parameters from a map.
         * 
         * @param parameters the parameters to add (cannot be null)
         * @return this builder
         * @throws IllegalArgumentException if parameters is null or contains null
         *                                  keys/values
         */
        public Builder parameters(Map<String, String> parameters) {
            if (parameters == null) {
                throw new IllegalArgumentException("Parameters map cannot be null");
            }
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException("Parameter key cannot be null");
                }
                if (entry.getValue() == null) {
                    throw new IllegalArgumentException("Parameter value cannot be null");
                }
            }
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * Removes a parameter.
         * 
         * @param key the parameter key to remove
         * @return this builder
         */
        public Builder removeParameter(String key) {
            this.parameters.remove(key);
            return this;
        }

        /**
         * Clears all parameters.
         * 
         * @return this builder
         */
        public Builder clearParameters() {
            this.parameters.clear();
            return this;
        }

        /**
         * Builds the search history entry.
         * 
         * @return a new SearchHistoryEntry instance
         * @throws IllegalStateException if required fields are not set
         */
        public SearchHistoryEntry build() {
            if (queryText == null) {
                throw new IllegalStateException("Query text is required");
            }
            if (queryType == null) {
                throw new IllegalStateException("Query type is required");
            }
            return new SearchHistoryEntry(date, queryText, queryType, caseSensitive, parameters);
        }
    }
}
