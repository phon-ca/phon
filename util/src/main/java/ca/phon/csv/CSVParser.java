package ca.phon.csv;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes data from a CSVTokenStream into arrays of Strings.
 * <p>Uses the following grammar:
 * <pre>
 *  csvfile
 *  :   record (NEWLINE record)* EOF
 *  ;
 *
 *  record
 *  :  field (SEPARATOR field)*
 *  ;
 *
 *  field
 *  :   escaped
 *  |   unescaped
 *  ;
 *
 *  escaped
 *  :   QUOTE (TEXTDATA | NEWLINE | SEPARATOR | TWO_QUOTE)* QUOTE
 *  ;
 *
 *  unescaped
 *  :   TEXTDATA*
 *  ;
 * </pre>
 * </p>
 * */
public class CSVParser {
    private final CSVTokenStream tokenStream;
    private final List<CSVParserListener> listenerList = new ArrayList<>();
    private final CSVQuoteType quoteChar;
    private final boolean trimSpaces;

    private int recordCount = 0;
    private int fieldCount = 0;

    /**
     * Constructs a new CSVParser with the specified token stream, quote character and boolean for trimming spaces
     *
     * @param tokenStream An object containing a queue of tokens to be parsed.
     * @param quoteChar An enum for which type of quote character is being used.
     * @param trimSpaces If leading and trailing whitespace should be trimmed.
     * */
    public CSVParser(CSVTokenStream tokenStream, CSVQuoteType quoteChar, boolean trimSpaces) {
        this.tokenStream = tokenStream;
        this.quoteChar = quoteChar;
        this.trimSpaces = trimSpaces;
    }

    /**
     * Adds a listener to the CSVParser.
     * */
    public void addListener(CSVParserListener listener) {
        listenerList.add(listener);
    }

    /**
     * Parses the tokens for an entire CSV file.
     * */
    public void csvFile() {
        listenerList.forEach(l -> l.startCSVFile(tokenStream));

        while (tokenStream.LA(1) != CSVTokenType.EOF) {
            record();
        }

        listenerList.forEach(l -> l.endCSVFile(tokenStream));
    }

    /**
     * Parses the tokens for a single record (row) from a CSV file.
     * @return An array of strings containing the contents of each field in the record.
     * */
    public String[] record() {
        fieldCount = 0;
        listenerList.forEach(l -> l.startRecord(tokenStream, recordCount));
        ArrayList<String> retVal = new ArrayList<>();

        while (tokenStream.LA(1) != CSVTokenType.EOF && tokenStream.LA(1) != CSVTokenType.NEWLINE) {
            String field = field();
            retVal.add(field);
            if (tokenStream.LA(1) == CSVTokenType.SEPARATOR) {
                tokenStream.nextToken();
                // check for empty field at end of record
                if(tokenStream.LA(1) == CSVTokenType.NEWLINE) {
                    retVal.add("");
                }
            }
        }

        if (tokenStream.LA(1) == CSVTokenType.NEWLINE) {
            tokenStream.nextToken();
        }

        String[] retValArray = retVal.isEmpty() ? null : retVal.toArray(String[]::new);
        for (CSVParserListener listener : listenerList) {
            listener.endRecord(tokenStream, recordCount, retValArray);
        }
        recordCount++;
        return retValArray;
    }

    /**
     * Parses the tokens for a single field (cell) from a CSV file.
     * @return The contents of the field as a string.
     * */
    public String field() {
        listenerList.forEach(l -> l.startField(tokenStream, recordCount, fieldCount));
        String retVal = "";
        if(tokenStream.LA(1) == CSVTokenType.QUOTE) {
            retVal = escaped();
        } else {
            retVal = unescaped();
        }
        if (this.trimSpaces) {
            retVal = retVal.trim();
        }
        for(CSVParserListener listener:listenerList) {
            listener.endField(tokenStream, recordCount, fieldCount, retVal);
        }
        fieldCount++;
        return retVal;
    }

    /**
     * Parses the tokens for an escaped field (cell surrounded by quotes) from a CSV file.
     * @return The escaped contents of the field as a string.
     * */
    public String escaped() {
        listenerList.forEach(l -> l.startEscaped(tokenStream, recordCount, fieldCount));
        if(tokenStream.LA(1) != CSVTokenType.QUOTE) {
            throw new IllegalStateException("Should be quote");
        }
        tokenStream.nextToken();

        final StringBuilder builder = new StringBuilder();
        while(tokenStream.LA(1) != CSVTokenType.EOF) {
            CSVToken token = tokenStream.nextToken();
            if (token.getType() == CSVTokenType.QUOTE) {
                if (tokenStream.LA(1) == CSVTokenType.QUOTE) {
                    builder.append(quoteChar == CSVQuoteType.SINGLE_QUOTE ? '\'' : '"');
                    tokenStream.nextToken();
                }
                else {
                    break;
                }
            }
            else {
                builder.append(token.getText());
            }
        }
        listenerList.forEach(l -> l.endEscaped(tokenStream, recordCount, fieldCount, builder.toString()));
        return builder.toString();
    }

    /**
     * Parses the tokens for an unescaped field (cell not surrounded by quotes) from a CSV file.
     * @return The contents of the field as a string.
     * */
    public String unescaped() {
        listenerList.forEach(l -> l.startUnescaped(tokenStream, recordCount, fieldCount));
        final StringBuilder builder = new StringBuilder();
        while(tokenStream.LA(1) == CSVTokenType.TEXT_DATA) {
            builder.append(tokenStream.nextToken().getText());
        }
        listenerList.forEach(l -> l.endUnescaped(tokenStream, recordCount, fieldCount, builder.toString()));
        return builder.toString();
    }
}
