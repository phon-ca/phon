package ca.phon.app.csv;

import ca.phon.session.Session;

/**
 * Listener for the CSVImporter object.
 * @author Cristopher Yates
 * */
public interface CSVImporterListener {
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
