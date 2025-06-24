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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a column in a CSV file with associated metadata and options.
 * Each column has a type, index, and configurable options.
 */
public class CSVColumn {
    private int csvColumnIndex;
    private CSVColumnType columnType;
    private final Map<String, String> options = new HashMap<>();
    private boolean importThisColumn = true;
    private final String id;

    public CSVColumn() {
        id = UUID.randomUUID().toString();
    }

    /**
     * Get the CSV column index.
     * 
     * @return the column index
     */
    public int getCsvColumnIndex() {
        return csvColumnIndex;
    }

    /**
     * Set the CSV column index.
     * 
     * @param csvColumnIndex the column index
     */
    public void setCsvColumnIndex(int csvColumnIndex) {
        this.csvColumnIndex = csvColumnIndex;
    }

    /**
     * Get the column type.
     * 
     * @return the column type
     */
    public CSVColumnType getColumnType() {
        return columnType;
    }

    /**
     * Set the column type.
     * 
     * @param columnType the column type
     */
    public void setColumnType(CSVColumnType columnType) {
        this.columnType = columnType;
    }

    /**
     * Check if this column should be imported.
     * 
     * @return true if the column should be imported
     */
    public boolean shouldImportThisColumn() {
        return importThisColumn;
    }

    /**
     * Set whether this column should be imported.
     * 
     * @param importThisColumn true if the column should be imported
     */
    public void setImportThisColumn(boolean importThisColumn) {
        this.importThisColumn = importThisColumn;
    }

    /**
     * Get the unique identifier for this column.
     * 
     * @return the unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * Set an option value for this column.
     * 
     * @param name  the option name
     * @param value the option value
     */
    public void setOption(String name, String value) {
        options.put(name, value);
    }

    /**
     * Get an option value for this column.
     * 
     * @param name the option name
     * @return the option value, or null if not set
     */
    public String getOption(String name) {
        return options.get(name);
    }
}
