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
package ca.phon.app.csv;

import ca.phon.csv.CSVQuoteType;
import ca.phon.session.filter.RecordFilter;

import java.util.*;

/**
 * Configuration settings for CSV export operations.
 * Contains column mappings, formatting options, and export parameters.
 */
public class CSVExportSettings {
    private final Map<String, Integer> userTiers;
    public final static String USER_TIER_NAME_KEY = "tierName";
    private boolean useFirstRowAsHeader = true;
    private char separator;
    private CSVQuoteType quoteType;
    private boolean trimSpaces;
    private String encoding;
    
    private RecordFilter recordFilter;

    private List<CSVColumn> exportColumnList = new ArrayList<>();

    public CSVExportSettings(char separator, CSVQuoteType quoteType, String encoding, boolean useFirstRowAsHeader) {
        this.separator = separator;
        this.quoteType = quoteType;
        this.encoding = encoding;
        this.useFirstRowAsHeader = useFirstRowAsHeader;
        this.userTiers = new HashMap<>();
    }

    public void addTier(CSVColumnType type, int columnIndex) {
        var exportColumn = new CSVColumn();
        exportColumn.setColumnType(type);
        exportColumn.setCsvColumnIndex(columnIndex);
        exportColumnList.add(exportColumn);
    }

    public Map<String, Integer> getUserTiers() {
        return userTiers;
    }

    public void addUserTier(String tierName, int tierTierIndex) {
        var exportColumn = new CSVColumn();
        exportColumn.setCsvColumnIndex(tierTierIndex);
        exportColumn.setColumnType(CSVColumnType.USER_TIER);
        exportColumn.setOption(USER_TIER_NAME_KEY, tierName);
        exportColumnList.add(exportColumn);
    }

    public boolean isUseFirstRowAsHeader() {
        return useFirstRowAsHeader;
    }

    public void setUseFirstRowAsHeader(boolean useFirstRowAsHeader) {
        this.useFirstRowAsHeader = useFirstRowAsHeader;
    }

    public List<CSVColumn> getExportColumnList() {
        return Collections.unmodifiableList(exportColumnList);
    }

    public void setExportColumnList(List<CSVColumn> columnList) {
        exportColumnList = columnList;
    }

    /**
     * Get the CSV separator character.
     * 
     * @return the separator character
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Get the CSV quote type.
     * 
     * @return the quote type
     */
    public CSVQuoteType getQuoteType() {
        return quoteType;
    }

    /**
     * Check if spaces should be trimmed.
     * 
     * @return true if spaces should be trimmed
     */
    public boolean isTrimSpaces() {
        return trimSpaces;
    }

    /**
     * Get the encoding.
     * 
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }
    
    /**
     * Get the record filter.
     * 
     * @return the record filter
     */
    public RecordFilter getRecordFilter() {
        return recordFilter;
    }
    
    /**
     * Set the record filter.
     * 
     * @param recordFilter the record filter
     */
    public void setRecordFilter(RecordFilter recordFilter) {
        this.recordFilter = recordFilter;
    }
}
