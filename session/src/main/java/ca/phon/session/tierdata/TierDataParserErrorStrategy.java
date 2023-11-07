package ca.phon.session.tierdata;

import ca.phon.ipa.parser.exceptions.IPAParserException;
import org.antlr.v4.runtime.*;

public class TierDataParserErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
        super.reportError(recognizer, e);
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw new TierDataParserException(TierDataParserException.Type.InvalidToken, "Invalid token", e.getOffendingToken().getCharPositionInLine());
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new InputMismatchException(recognizer);
    }

}
