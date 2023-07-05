package ca.phon.csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

// https://datatracker.ietf.org/doc/html/rfc4180

/**
 * <p>Reads data from a CSV file with specified settings.</p>
 * <p>We escape fields based on the rules defined in
 * <a href="https://datatracker.ietf.org/doc/html/rfc4180">RFC4180</a>.
 * </p>
 * @author Cristopher Yates
 * */
public class CSVReader implements AutoCloseable, Closeable {
    private final BufferedReader reader;
    private final boolean trimSpaces;
    private final static boolean DEFAULT_TRIM_SPACES = false;
    private final char[] separators;
    private final static Character DEFAULT_SEPARATOR = ',';
    private final CSVQuoteType quoteChar;
    private final static CSVQuoteType DEFAULT_QUOTE_CHAR = CSVQuoteType.DOUBLE_QUOTE;
    private final CSVParser csvParser;

    /**
     * Constructs a new CSVReader.
     * @param reader The reader that the data is read from.
     * */
    public CSVReader(BufferedReader reader) {
        this(reader, new char[]{ DEFAULT_SEPARATOR }, DEFAULT_QUOTE_CHAR, DEFAULT_TRIM_SPACES);
    }

    public CSVReader(BufferedReader reader, char[] separators) {
        this(reader, separators, DEFAULT_QUOTE_CHAR, DEFAULT_TRIM_SPACES);
    }
    public CSVReader(BufferedReader reader, CSVQuoteType quoteChar) {
        this(reader, new char[]{ DEFAULT_SEPARATOR }, quoteChar, DEFAULT_TRIM_SPACES);
    }
    public CSVReader(BufferedReader reader, boolean trimSpaces) {
        this(reader, new char[]{ DEFAULT_SEPARATOR }, DEFAULT_QUOTE_CHAR, trimSpaces);
    }

    public CSVReader(Reader reader, char[] separators, CSVQuoteType quoteChar) {
        this(reader, separators, quoteChar, DEFAULT_TRIM_SPACES);
    }
    public CSVReader(Reader reader, char[] separators, boolean trimSpaces) {
        this(reader, separators, DEFAULT_QUOTE_CHAR, trimSpaces);
    }
    public CSVReader(Reader reader, CSVQuoteType quoteChar, boolean trimSpaces) {
        this(reader, new char[]{ DEFAULT_SEPARATOR }, quoteChar, trimSpaces);
    }

    /**
     * Constructs a new CSVReader.
     * @param reader The reader that the data is read from.
     * @param separators The characters that signal the end of a field and beginning of another.
     * @param quoteChar The type of quote used to enclose escaped fields (single or double).
     * @param trimSpaces Whether to remove any leading or trailing whitespace.
     * */
    public CSVReader(Reader reader, char[] separators, CSVQuoteType quoteChar, boolean trimSpaces) {
        this.reader = new BufferedReader(reader);
        this.separators = separators;
        this.quoteChar = quoteChar;
        this.trimSpaces = trimSpaces;

        CSVTokenStream tokenStream = new CSVTokenStream(this.reader, this.quoteChar, this.separators);
        this.csvParser = new CSVParser(tokenStream, quoteChar, this.trimSpaces);
    }


    @Override
    public void close() throws IOException {
        if (reader != null) reader.close();
    }

    /**
     * Reads the next record from the reader.
     * @return The record as a string array of the fields.
     * */
    public String[] readNext() throws IOException {
        String[] record = this.csvParser.record();
        return record;
    }
}
