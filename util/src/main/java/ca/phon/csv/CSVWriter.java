package ca.phon.csv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

// https://datatracker.ietf.org/doc/html/rfc4180

/**
 * <p>Writes data to a CSV file with specified settings.</p>
 * <p>We escape fields based on the rules defined in
 * <a href="https://datatracker.ietf.org/doc/html/rfc4180">RFC4180</a>.
 * </p>
 * @author Cristopher Yates
 * */
public class CSVWriter implements AutoCloseable, Closeable {
    private final BufferedWriter writer;
    private final boolean trimSpaces;
    private static final boolean DEFAULT_TRIM_SPACES = false;
    private final char separator;
    private static final char DEFAULT_SEPARATOR = ',';
    private final CSVQuoteType quoteType;
    private static final CSVQuoteType DEFAULT_QUOTE_TYPE = CSVQuoteType.DOUBLE_QUOTE;
    private final boolean unixLineEndings;
    private static final boolean DEFAULT_UNIX_LINE_ENDINGS = System.lineSeparator().equals("\n");
    private final boolean quoteAllFields;
    private static final boolean DEFAULT_QUOTE_ALL_FIELDS = false;

    private final List<CSVWriterListener> listenerList = new ArrayList<>();
    private int recordCount = 0;
    private int fieldCount = 0;

    /**
     * Constructs a new CSVWriter with the provided writer and default values for all other options.
     * @param writer The writer for writing the data to a specified file or buffer.
     * */
    public CSVWriter(Writer writer) {
        this(
            writer,
            DEFAULT_SEPARATOR,
            DEFAULT_QUOTE_TYPE,
            DEFAULT_TRIM_SPACES,
            DEFAULT_UNIX_LINE_ENDINGS,
            DEFAULT_QUOTE_ALL_FIELDS
        );
    }

    /**
     * Constructs a new CSVWriter.
     * @param writer The writer for writing the data to a specified file or
     *               buffer.
     * @param separator The character that will go between the fields.
     * @param quoteType The type of quote (single or double) used to surround
     *                  escaped fields.
     * @param trimSpaces Decides whether to trim whitespace from the starts
     *                   and ends of the fields.
     * @param unixLineEndings Decides whether to use Unix line endings (LF) or
     *                        Windows line endings (CRLF).
     * @param quoteAllFields Decides whether to force all fields to be enclosed
     *                       with quotes.
     * */
    public CSVWriter(
        Writer writer,
        char separator,
        CSVQuoteType quoteType,
        boolean trimSpaces,
        boolean unixLineEndings,
        boolean quoteAllFields
    ) {
        this.writer = new BufferedWriter(writer);
        this.separator = separator;
        this.quoteType = quoteType;
        this.trimSpaces = trimSpaces;
        this.unixLineEndings = unixLineEndings;
        this.quoteAllFields = quoteAllFields;
    }

    @Override
    public void close() throws IOException {
        if (writer != null) writer.close();
    }

    /**
     * Adds a listener to the CSVWriter.
     * */
    public void addListener(CSVWriterListener listener) {
        listenerList.add(listener);
    }

    /**
     * Write the provided data as the next line in a CSV.
     * @param row The data to be written as the next row.
     * */
    public void writeNext(String[] row) throws IOException {
        writeRecord(row);
    }

    /**
     * Writes the data of a record to the writer.
     * @param record The record to write.
     * */
    private void writeRecord(String[] record) throws IOException {
        fieldCount = 0;
        listenerList.forEach(l -> l.startRecord(writer, recordCount));
        for (int i = 0; i < record.length; i++) {
            if (i > 0) {
                this.writer.write(Character.toString(this.separator));
            }
            writeField(record[i]);
        }
        this.writer.write(this.unixLineEndings ? "\n" : "\r\n");
        for (CSVWriterListener listener : listenerList) {
            listener.endRecord(writer, recordCount, record);
        }
        recordCount++;
    }

    /**
     * Writes the data of a field to the writer.
     * @param field The data to write.
     * */
    private void writeField(String field) throws IOException {
        listenerList.forEach(l -> l.startField(writer, recordCount, fieldCount));
        String processedField = trimSpaces ? field.trim() : field;
        boolean containsQuoteChar = processedField.contains(Character.toString(this.quoteType.getQuoteChar()));
        boolean containsSeparator = processedField.contains(Character.toString(this.separator));
        boolean containsNewLine = processedField.contains(Character.toString('\n'));
        boolean forceEscaped = containsQuoteChar || containsSeparator || containsNewLine;
        if (this.quoteAllFields || forceEscaped) {
            writeEscaped(processedField);
        }
        else {
            writeUnescaped(processedField);
        }
        for (CSVWriterListener listener : listenerList) {
            listener.endField(writer, recordCount, fieldCount, field);
        }
        fieldCount++;
    }

    /**
     * Writes the data of an escaped field to the writer.
     * @param field The data to write as an escaped field.
     * */
    private void writeEscaped(String field) throws IOException {
        listenerList.forEach(l -> l.startEscaped(writer, recordCount, fieldCount));
        char c = this.quoteType.getQuoteChar();
        // Replace all instances of the quote character with two of that character
        field = field.replaceAll("" + c,"" + c + c);
        // Write the string, enclosed by the quote character, to the file
        this.writer.write(c + field + c);
        for (CSVWriterListener listener : listenerList) {
            listener.endEscaped(writer, recordCount, fieldCount, field);
        }
    }

    /**
     * Writes the data of an unescaped field to the writer
     * @param field The data to write as an unescaped field.
     * */
    private void writeUnescaped(String field) throws IOException {
        listenerList.forEach(l -> l.startUnescaped(writer, recordCount, fieldCount));
        // Write the string to the file
        this.writer.write(field);
        for (CSVWriterListener listener : listenerList) {
            listener.endUnescaped(writer, recordCount, fieldCount, field);
        }
    }
}
