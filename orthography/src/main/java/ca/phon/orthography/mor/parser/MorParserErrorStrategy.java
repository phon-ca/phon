package ca.phon.orthography.mor.parser;

import org.antlr.v4.runtime.*;

public class MorParserErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw new MorParserException(MorParserException.Type.InvalidToken, "Invalid token", e.getOffendingToken().getCharPositionInLine());
    }

    @Override
    protected void reportNoViableAlternative(Parser recognizer, NoViableAltException e) {
    }

    @Override
    protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
    }

    @Override
    protected void reportUnwantedToken(Parser recognizer) {
    }

    @Override
    protected void reportMissingToken(Parser recognizer) {
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new InputMismatchException(recognizer);
    }

}
