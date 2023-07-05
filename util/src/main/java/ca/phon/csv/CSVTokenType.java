package ca.phon.csv;

/**
 * The types CSV tokens.
 * @author Cristopher Yates
 * */
enum CSVTokenType {
    SEPARATOR,
    QUOTE,
    TEXT_DATA,
    NEWLINE,
    EOF;
}