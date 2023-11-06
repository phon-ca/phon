package ca.phon.orthography.parser;

import org.antlr.v4.runtime.*;

public class OrthoParserErrorStrategy extends DefaultErrorStrategy {

    private final UnicodeOrthographyBuilder builder;

    private final OrthoTokens orthoTokens = new OrthoTokens();

    public OrthoParserErrorStrategy(UnicodeOrthographyBuilder builder) {
        super();
        this.builder = builder;
    }

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        if(e.getOffendingToken() != null && e.getOffendingToken().getType() == Token.EOF) {
            if(e.getCtx() instanceof UnicodeOrthographyParser.GroupContext grpCtx) {
                throw new OrthoParserException(OrthoParserException.Type.MissingGroupEnd, "Missing group end", grpCtx.getStart().getCharPositionInLine());
            } else if(e.getCtx() instanceof UnicodeOrthographyParser.Phonetic_groupContext pgCtx) {
                throw new OrthoParserException(OrthoParserException.Type.MissingPgEnd, "Missing phonetic group end", pgCtx.getStart().getCharPositionInLine());
            }
        } else if(e.getOffendingToken() != null && e.getOffendingToken().getType() == orthoTokens.getTokenType("GREATER_THAN")) {
            throw new OrthoParserException(OrthoParserException.Type.MissingGroupStart, "Missing group start", e.getOffendingToken().getCharPositionInLine());
        } else if(e.getOffendingToken() != null && e.getOffendingToken().getType() == orthoTokens.getTokenType("PG_END")) {
            throw new OrthoParserException(OrthoParserException.Type.MissingPgStart, "Missing phonetic group start", e.getOffendingToken().getCharPositionInLine());
        }
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new InputMismatchException(recognizer);
    }

}
