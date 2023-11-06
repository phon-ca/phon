package ca.phon.orthography.parser;

import org.antlr.v4.runtime.*;

public class OrthoParserErrorStrategy extends DefaultErrorStrategy {

    private final UnicodeOrthographyBuilder builder;

    public OrthoParserErrorStrategy(UnicodeOrthographyBuilder builder) {
        super();
        this.builder = builder;
    }

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        // TODO
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new InputMismatchException(recognizer);
    }

}
