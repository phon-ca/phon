package ca.phon.session.tierdata;

import org.antlr.v4.runtime.*;

public class TierDataParserErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        if(e.getCtx() instanceof TierDataParser.Time_in_minutes_secondsContext timeContext) {
            throw new TierDataParserException(TierDataParserException.Type.MissingMediaBullet, "Missing media bullet", timeContext.getStart().getCharPositionInLine());
        } else if(e.getCtx() instanceof TierDataParser.Internal_mediaContext internalMediaContext) {
            if (e.getOffendingToken().getText().isBlank()) {
                throw new TierDataParserException(TierDataParserException.Type.MissingMediaBullet, "Missing media bullet", e.getOffendingToken().getCharPositionInLine());
            }
        }
        throw new TierDataParserException(TierDataParserException.Type.InvalidToken, "Invalid token", e.getOffendingToken().getCharPositionInLine());
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new InputMismatchException(recognizer);
    }

}
