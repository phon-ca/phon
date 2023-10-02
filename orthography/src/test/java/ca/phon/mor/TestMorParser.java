package ca.phon.mor;

import ca.phon.orthography.TerminatorType;
import ca.phon.orthography.mor.Mor;
import ca.phon.orthography.mor.MorTerminator;
import ca.phon.orthography.mor.parser.MorBuilder;
import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class TestMorParser {

    private Mor roundTrip(String text) throws ParseException {
        CharStream charStream = CharStreams.fromString(text);
        MorLexer lexer = new MorLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        MorBuilder listener = new MorBuilder();
        MorParser parser = new MorParser(tokenStream);
        parser.addParseListener(listener);

        try {
            parser.start();
            final String rt = listener.getMors().stream().map(Object::toString).collect(Collectors.joining(" "));
            Assert.assertEquals(text, rt);
        } catch(RecognitionException e) {
            throw new ParseException(e.getLocalizedMessage(), e.getOffendingToken().getCharPositionInLine());
        }
        return new Mor(new MorTerminator(TerminatorType.PERIOD), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);
    }

    @Test
    public void testMorWord() throws ParseException {
        final String text = "det|the n|people v:aux|be&PRES v|make-ING n|cake-PL .";
        final Mor mor = roundTrip(text);
    }

    @Test
    public void testMorClitics() throws ParseException {
        final String text = "pro|you part|go-PROG~inf|to v|put&ZERO det|the " +
                "n|+on|choo+on|choo~v:cop|be&3S n|wheel adv:loc|on ?";
        final Mor mor = roundTrip(text);
    }

    @Test
    public void testMorPost() throws ParseException {
        final String text = "pro|it~v|be&3S pro|me !";
        final Mor mor = roundTrip(text);
    }

}
