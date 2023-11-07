package ca.phon.orthography.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

public class OrthoParserErrorListener extends BaseErrorListener {

    private List<OrthoParserException> parseExceptions = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        OrthoParserException ex = new OrthoParserException(OrthoParserException.Type.InvalidToken, msg, charPositionInLine);
        parseExceptions.add(ex);
    }

    public List<OrthoParserException> getParseExceptions() {
        return this.parseExceptions;
    }

}
