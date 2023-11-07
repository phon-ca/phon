package ca.phon.session.tierdata;

import org.antlr.v4.runtime.*;

public class TierDataParserErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
    }

    @Override
    protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
    }

    @Override
    protected void reportMissingToken(Parser recognizer) {
    }

    @Override
    protected void reportUnwantedToken(Parser recognizer) {
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        if(e.getOffendingToken() != null && e.getOffendingToken().getType() == Token.EOF) {
            if(e.getCtx() instanceof TierDataParser.CommentContext commentContext) {
                throw new TierDataParserException(TierDataParserException.Type.MissingCloseBracket, "Missing close bracket", commentContext.getStart().getCharPositionInLine());
            } else if(e.getCtx() instanceof TierDataParser.LinkContext linkContext) {
                throw new TierDataParserException(TierDataParserException.Type.MissingCloseBracket, "Missing close bracket", linkContext.getStart().getCharPositionInLine());
            }
        } else if(e.getCtx() instanceof TierDataParser.Time_in_minutes_secondsContext timeContext) {
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
