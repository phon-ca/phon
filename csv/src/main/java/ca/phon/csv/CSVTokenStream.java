package ca.phon.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A tokenizer and queue of tokens as read from a CSV file.
 * @author Cristopher Yates
 * */
public class CSVTokenStream {
    private final Reader reader;
    private final CSVQuoteType quoteType;
    private final HashSet<Character> separators;
    private final Queue<CSVToken> tokenQueue;

    private int currentCharPos = 0;
    private int currentCharPosInLine = 0;
    private int lineNumber = 0;

    /**
     * Constructs a new CSVTokenStream.
     * @param reader The reader for reading the data from a specified file or
     *               buffer.
     * @param quoteType An enum for which type of quote character is being
     *                  used.
     * @param separators An array of characters that act as separators in the
     *                   CSV.
     * */
    public CSVTokenStream(
        Reader reader,
        CSVQuoteType quoteType,
        char[] separators
    ) {
        super();
        this.reader = reader;
        this.quoteType = quoteType;

        this.tokenQueue = new LinkedList<>();

        this.separators = new HashSet<>();
        for (char c : separators) {
            this.separators.add(c);
        }
    }

    /**
     * Reads in the next character from the reader and adds the appropriate
     * token to the queue.
     * */
    private void readNext() {
        try {
            // Read the data from the reader
            int readData = reader.read();

            boolean newLine = false;
            // If you're at the end of the file
            if (readData == -1) {
                this.tokenQueue.add(new CSVToken(
                    CSVTokenType.EOF,
                    currentCharPos,
                    currentCharPosInLine,
                    0,
                    lineNumber
                ));
                return;
            }

            char c = (char)readData;
            char quoteChar = quoteType.getQuoteChar();

            // If the character is a newline
            if (c == '\n') {
                this.tokenQueue.add(new CSVToken(
                    CSVTokenType.NEWLINE,
                    "\n",
                    currentCharPos,
                    currentCharPosInLine,
                    1,
                    lineNumber
                ));
                newLine = true;
            }
            // If the character is a carriage return
            else if (c == '\r') {
                char nextChar = (char)reader.read();
                if (nextChar != '\n') {
                    throw new IllegalStateException("Expected newline");
                }

                // Add a newline token
                this.tokenQueue.add(new CSVToken(
                    CSVTokenType.NEWLINE,
                    "\r\n",
                    currentCharPos,
                    currentCharPosInLine,
                    2,
                    lineNumber
                ));
                currentCharPos++;
                newLine = true;
            }
            // If the character is one of the separators
            else if (separators.contains(c)) {
                // Add a separator token
                this.tokenQueue.add(new CSVToken(
                    CSVTokenType.SEPARATOR,
                    Character.toString(c),
                    currentCharPos,
                    currentCharPosInLine,
                    1,
                    lineNumber
                ));
            }
            // If the character is a quote
            else if (c == quoteChar) {
                this.tokenQueue.add(new CSVToken(
                    CSVTokenType.QUOTE,
                    currentCharPos,
                    currentCharPosInLine,
                    1,
                    lineNumber
                ));
            }
            // If the character is anything else
            else {
                // Add a text data token with that character as the text
                this.tokenQueue.add(new CSVToken(
                    CSVTokenType.TEXT_DATA,
                    Character.toString(c),
                    currentCharPos,
                    currentCharPosInLine,
                    1,
                    lineNumber
                ));
            }

            // Increment the current character position
            currentCharPos++;
            // If this token started a new line
            if (newLine) {
                // Set the character position in line to 0 and increment the line counter
                currentCharPosInLine = 0;
                lineNumber++;
            }
            else {
                // Otherwise increment the character position in line
                currentCharPosInLine++;
            }
        }
        // If an IO exception happens at any point in this function
        catch (IOException e) {
            // Add an end of file token
            this.tokenQueue.add(new CSVToken(
                CSVTokenType.EOF,
                currentCharPos,
                currentCharPosInLine,
                0,
                lineNumber
            ));
        }
    }

    /**
     * Gets the next token from the queue.
     * @return The next token from the queue.
     * */
    public CSVToken nextToken() {
        if (tokenQueue.isEmpty()) {
            readNext();
        }
        return tokenQueue.poll();
    }

    /**
     * Get the type of the token n spaces ahead in the queue without removing it.
     * @param n The number of places in the queue to look ahead.
     * @return The type of the token at position n - 1 ≈ in the queue.
     * */
    public CSVTokenType LA(int n) {
        int startingQueueSize = tokenQueue.size();
        for (int i = 0; i < n - startingQueueSize; i++) {
            readNext();
        }
        CSVToken[] tokenQueueArray = this.tokenQueue.toArray(new CSVToken[tokenQueue.size()]);
        return tokenQueueArray[n-1].getType();
    }

    /**
     * Get the token n spaces ahead in the queue without removing it.
     * @param n The number of places in the queue to look ahead.
     * @return The token at position n - 1 in the queue.
     * */
    public CSVToken LT(int n) {
        int startingQueueSize = tokenQueue.size();
        for (int i = 0; i < n - startingQueueSize; i++) {
            readNext();
        }
        CSVToken[] tokenQueueArray = this.tokenQueue.toArray(new CSVToken[tokenQueue.size()]);
        return tokenQueueArray[n-1];
    }

    public boolean add(CSVToken token) {
        return tokenQueue.add(token);
    }
}
