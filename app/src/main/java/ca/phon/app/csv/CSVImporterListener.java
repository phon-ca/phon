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

import ca.phon.session.Session;

/**
 * Listener for the CSVImporter object.
 */
public interface CSVImporterListener {

    /**
     * Called when a CSV file has been successfully imported.
     *
     * @param fileName csv file name
     */
    void importComplete(String fileName);

    /**
     * Called when a new session is created from a CSV file.
     *
     * @param fileName csv file name
     * @param session  the new session created
     */
    void sessionCreated(String fileName, Session session);

    /**
     * Called when a CSV file has ae parsing error.
     *
     * @param fileName             csv file name
     * @param csvRecordIndex       row index in the CSV file
     * @param fieldIndex           column index in the CSV file
     * @param charPositionInField  character position in the field
     * @param csvColumnType        type of the column
     * @param session              the session being imported into
     * @param recordIndexInSession index of the record in the session
     * @param e                    the exception that was thrown
     */
    void parsingError(
            String fileName,
            int csvRecordIndex,
            int fieldIndex,
            int charPositionInField,
            CSVColumnType csvColumnType,
            Session session,
            int recordIndexInSession,
            Exception e);
}
