package ca.phon.app.csv;

import ca.phon.session.Session;

/**
 * Listener for the CSVImporter object.
 * */
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
     * @param session the new session created
     */
    void sessionCreated(String fileName, Session session);

    /**
     * Called when a CSV file has ae parsing error.
     *
     * @param fileName csv file name
     * @param csvRecordIndex row index in the CSV file
     * @param fieldIndex column index in the CSV file
     * @param charPositionInField character position in the field
     * @param csvColumnType type of the column
     * @param session the session being imported into
     * @param recordIndexInSession index of the record in the session
     * @param e the exception that was thrown
     */
    void parsingError(
        String fileName,
        int csvRecordIndex,
        int fieldIndex,
        int charPositionInField,
        CSVColumnType csvColumnType,
        Session session,
        int recordIndexInSession,
        Exception e
    );
}
