package ca.phon.orthography.mor.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

public class MorParserErrorListener extends BaseErrorListener {

    private List<MorParserException> parseExceptions = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        MorParserException ex = new MorParserException(MorParserException.Type.InvalidToken, msg, charPositionInLine);
        parseExceptions.add(ex);
    }

    public List<MorParserException> getParseExceptions() {
        return this.parseExceptions;
    }

}
