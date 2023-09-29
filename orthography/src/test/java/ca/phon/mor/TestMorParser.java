package ca.phon.mor;

import ca.phon.orthography.mor.parser.MorBuilder;
import org.antlr.v4.runtime.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class TestMorParser {

    @Test
    public void testMorWord() {
        final String text = "det|the n|people v:aux|be&PRES v|make-ING n|cake-PL .";

        CharStream charStream = CharStreams.fromString(text);
        MorLexer lexer = new MorLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        MorBuilder listener = new MorBuilder();
        MorParser parser = new MorParser(tokenStream);
        parser.addParseListener(listener);

        try {
            parser.start();

            final String rt = listener.getElements().stream().map(Object::toString).collect(Collectors.joining(" "));
            System.out.println(rt);
        } catch(RecognitionException e) {
            throw new RuntimeException(e);
        }
    }

}
