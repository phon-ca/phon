package ca.phon.mor;

import ca.phon.orthography.mor.parser.MorParserListener;
import org.antlr.v4.runtime.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestMorParser {

    @Test
    public void testMorWord() {
        final String text = "det|the n|people v:aux|be&PRES v|make-ING n|cake-PL .";

        CharStream charStream = CharStreams.fromString(text);
        MorLexer lexer = new MorLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        MorParserListener listener = new MorParserListener();
        MorParser parser = new MorParser(tokenStream);
        parser.addParseListener(listener);

        try {
            parser.start();
        } catch(RecognitionException e) {
            throw new RuntimeException(e);
        }
    }

}
