package ca.phon.csv;

/**
 * Listener for the CSVParser object.
 * @author Cristopher Yates
 * */
public interface CSVParserListener {
    void startCSVFile(CSVTokenStream tokenStream);
    void endCSVFile(CSVTokenStream tokenStream);

    void startRecord(CSVTokenStream tokenStream, int recordIndex);
    void endRecord(CSVTokenStream tokenStream, int recordIndex, String[] record);

    void startField(CSVTokenStream tokenStream, int recordIndex, int fieldIndex);
    void endField(CSVTokenStream tokenStream, int recordIndex, int fieldIndex, String field);

    void startEscaped(CSVTokenStream tokenStream, int recordIndex, int fieldIndex);
    void endEscaped(CSVTokenStream tokenStream, int recordIndex, int fieldIndex, String escaped);

    void startUnescaped(CSVTokenStream tokenStream, int recordIndex, int fieldIndex);
    void endUnescaped(CSVTokenStream tokenStream, int recordIndex, int fieldIndex, String unescaped);
}
