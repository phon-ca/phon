package ca.phon.orthography;

import ca.phon.orthography.parser.UnicodeOrthographyLexer;
import ca.phon.orthography.parser.UnicodeOrthographyBuilder;
import ca.phon.orthography.parser.UnicodeOrthographyParser;
import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestUnicodeOrthography {

    private Orthography roundTrip(String text) {
        CharStream charStream = CharStreams.fromString(text);
        UnicodeOrthographyLexer lexer = new UnicodeOrthographyLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);

        UnicodeOrthographyBuilder orthoBuilder = new UnicodeOrthographyBuilder();
        UnicodeOrthographyParser parser = new UnicodeOrthographyParser(tokenStream);
        parser.addParseListener(orthoBuilder);
        parser.start();

        Orthography retVal = orthoBuilder.getOrthography();
        Assert.assertEquals(text, retVal.toString());
        return retVal;
    }

    @Test
    public void testWord() {
        final String text = "word";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
    }

    @Test
    public void testTagMarker() {
        for(TagMarkerType tmType:TagMarkerType.values()) {
            final String text = "wo " + tmType.getChar() + " rd";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(3, ortho.length());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
            Assert.assertEquals("wo", ortho.elementAt(0).toString());
            Assert.assertEquals(TagMarker.class, ortho.elementAt(1).getClass());
            Assert.assertEquals(tmType.getChar()+"", ortho.elementAt(1).toString());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(2).getClass());
            Assert.assertEquals("rd", ortho.elementAt(2).toString());
        }
    }

    @Test
    public void testTerminator() {
        for(TerminatorType tt:TerminatorType.values()) {
            final String text = "word " + tt.toString();
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(2, ortho.length());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
            Assert.assertEquals("word", ortho.elementAt(0).toString());
            Assert.assertEquals(Terminator.class, ortho.elementAt(1).getClass());
            Assert.assertEquals(tt.toString(), ortho.elementAt(1).toString());
        }
    }

}
