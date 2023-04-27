package ca.phon.orthography;

import ca.phon.orthography.parser.UnicodeOrthographyLexer;
import ca.phon.orthography.parser.UnicodeOrthographyBuilder;
import ca.phon.orthography.parser.UnicodeOrthographyParser;
import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

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
    public void testLinkers() {
        for(LinkerType lt:LinkerType.values()) {
            final String text = lt.getText() + " word";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(2, ortho.length());
            Assert.assertEquals(Linker.class, ortho.elementAt(0).getClass());
            Assert.assertEquals(lt.toString(), ortho.elementAt(0).toString());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(1).getClass());
            Assert.assertEquals("word", ortho.elementAt(1).toString());
        }
    }

    @Test
    public void testWord() {
        final String text = "word";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
    }

    @Test
    public void testCaElements() {
        for(CaElementType eleType:CaElementType.values()) {
            final String text = "wo" + eleType.toString() + "rd";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
            Assert.assertEquals("word", ((OrthoWord)ortho.elementAt(0)).getWord());
            final List<OrthoWordElement> wordElements = ((OrthoWord)ortho.elementAt(0)).getWordElements();
            Assert.assertEquals(3, wordElements.size());
            Assert.assertEquals(OrthoWordText.class, wordElements.get(0).getClass());
            Assert.assertEquals("wo", wordElements.get(0).getText());
            Assert.assertEquals(CaElement.class, wordElements.get(1).getClass());
            Assert.assertEquals(eleType.toString(), wordElements.get(1).getText());
            Assert.assertEquals(OrthoWordText.class, wordElements.get(2).getClass());
            Assert.assertEquals("rd", wordElements.get(2).getText());
        }
    }

    @Test
    public void testCaDelimiters() {
        for(CaDelimiterType delimType:CaDelimiterType.values()) {
            final String text = delimType.toString() + "word" + delimType.toString();
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
            Assert.assertEquals("word", ((OrthoWord)ortho.elementAt(0)).getWord());
            final List<OrthoWordElement> wordElements = ((OrthoWord)ortho.elementAt(0)).getWordElements();
            Assert.assertEquals(3, wordElements.size());
            Assert.assertEquals(CaDelimiter.class, wordElements.get(0).getClass());
            Assert.assertEquals(delimType.toString(), wordElements.get(0).getText());
            Assert.assertEquals(OrthoWordText.class, wordElements.get(1).getClass());
            Assert.assertEquals("word", wordElements.get(1).getText());
            Assert.assertEquals(CaDelimiter.class, wordElements.get(2).getClass());
            Assert.assertEquals(delimType.toString(), wordElements.get(2).getText());
        }
    }

    @Test
    public void testTagMarkers() {
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
    public void testTerminators() {
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
