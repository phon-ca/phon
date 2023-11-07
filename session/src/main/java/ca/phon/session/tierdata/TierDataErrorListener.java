package ca.phon.session.tierdata;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

public class TierDataErrorListener extends BaseErrorListener {

    private List<TierDataParserException> parseExceptions = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        TierDataParserException ex = new TierDataParserException(TierDataParserException.Type.InvalidToken, msg, charPositionInLine);
        parseExceptions.add(ex);
    }

    public List<TierDataParserException> getParseExceptions() {
        return this.parseExceptions;
    }

}
