package ca.phon.csv;

import java.io.Writer;

/**
 * Listener for the CSVWriter object.
 * @author Cristopher Yates
 * */
public interface CSVWriterListener {
    void startCSVFile(Writer writer);
    void endCSVFile(Writer writer);

    void startRecord(Writer writer, int recordIndex);
    void endRecord(Writer writer, int recordIndex, String[] record);

    void startField(Writer writer, int recordIndex, int fieldIndex);
    void endField(Writer writer, int recordIndex, int fieldIndex, String field);

    void startEscaped(Writer writer, int recordIndex, int fieldIndex);
    void endEscaped(Writer writer, int recordIndex, int fieldIndex, String escaped);

    void startUnescaped(Writer writer, int recordIndex, int fieldIndex);
    void endUnescaped(Writer writer, int recordIndex, int fieldIndex, String unescaped);
}
