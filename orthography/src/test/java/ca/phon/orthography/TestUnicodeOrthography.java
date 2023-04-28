package ca.phon.orthography;

import ca.phon.orthography.parser.UnicodeOrthographyLexer;
import ca.phon.orthography.parser.UnicodeOrthographyBuilder;
import ca.phon.orthography.parser.UnicodeOrthographyParser;
import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.swing.*;
import java.util.List;

/**
 * Parser tests for UnicodeOrthography.g4 grammar
 *
 */
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
    public void testCompoundWord() {
        for(OrthoCompoundWordMarkerType type:OrthoCompoundWordMarkerType.values()) {
            final String text = "hello" + type.getMarker() + "world";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(OrthoCompoundWord.class, ortho.elementAt(0).getClass());
            final OrthoCompoundWord compoundWord = (OrthoCompoundWord) ortho.elementAt(0);
            Assert.assertEquals("hello", compoundWord.getWord1().toString());
            Assert.assertEquals("world", compoundWord.getWord2().toString());
            Assert.assertEquals(type, compoundWord.getMarker().getType());
        }
    }

    @Test
    public void testOverlapPoints() {
        final String[] texts = {
            "this " + OverlapPointType.TOP_START.getChar() + " is a " + OverlapPointType.TOP_END.getChar() + " test",
            "this " + OverlapPointType.BOTTOM_START.getChar() + " is a " + OverlapPointType.BOTTOM_END.getChar() + " test"
        };
        for(String text:texts) {
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(6, ortho.length());
            Assert.assertEquals(OverlapPoint.class, ortho.elementAt(1).getClass());
            Assert.assertEquals(OverlapPoint.class, ortho.elementAt(4).getClass());
        }
    }

    @Test
    public void testIndexedOverlapPoints() {
        final String[] texts = {
                "this " + OverlapPointType.TOP_START.getChar() + "1 is a " + OverlapPointType.TOP_END.getChar() + "1 test",
                "this " + OverlapPointType.BOTTOM_START.getChar() + "2 is a " + OverlapPointType.BOTTOM_END.getChar() + "2 test"
        };
        for(int i = 0; i < texts.length; i++) {
            final String text = texts[i];
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(6, ortho.length());
            Assert.assertEquals(OverlapPoint.class, ortho.elementAt(1).getClass());
            Assert.assertEquals(i+1, ((OverlapPoint)ortho.elementAt(1)).getIndex());
            Assert.assertEquals(OverlapPoint.class, ortho.elementAt(4).getClass());
            Assert.assertEquals(i+1, ((OverlapPoint)ortho.elementAt(4)).getIndex());
        }
    }

    @Test
    public void testOverlapPointsInWord() {
        final String[] texts = {
                "this" + OverlapPointType.TOP_START.getChar() + " is a " + OverlapPointType.TOP_END.getChar() + "test",
                "this" + OverlapPointType.BOTTOM_START.getChar() + " is a " + OverlapPointType.BOTTOM_END.getChar() + "test"
        };
        for(String text:texts) {
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(4, ortho.length());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
            Assert.assertEquals(OverlapPoint.class, ((OrthoWord)ortho.elementAt(0)).getWordElements().get(1).getClass());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(3).getClass());
            Assert.assertEquals(OverlapPoint.class, ((OrthoWord)ortho.elementAt(3)).getWordElements().get(0).getClass());
        }
    }

    @Test
    public void testIndexedOverlapPointsInWord() {
        final String[] texts = {
                "this" + OverlapPointType.TOP_START.getChar() + "1 is a " + OverlapPointType.TOP_END.getChar() + "1test",
                "this" + OverlapPointType.BOTTOM_START.getChar() + "2 is a " + OverlapPointType.BOTTOM_END.getChar() + "2test"
        };
        for(int i = 0; i < texts.length; i++) {
            final String text = texts[i];
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(4, ortho.length());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
            Assert.assertEquals(OverlapPoint.class, ((OrthoWord)ortho.elementAt(0)).getWordElements().get(1).getClass());
            Assert.assertEquals(i+1, ((OverlapPoint)((OrthoWord)ortho.elementAt(0)).getWordElements().get(1)).getIndex());
            Assert.assertEquals(OrthoWord.class, ortho.elementAt(3).getClass());
            Assert.assertEquals(OverlapPoint.class, ((OrthoWord)ortho.elementAt(3)).getWordElements().get(0).getClass());
            Assert.assertEquals(i+1, ((OverlapPoint)((OrthoWord)ortho.elementAt(3)).getWordElements().get(0)).getIndex());
        }
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
            Assert.assertEquals("wo", wordElements.get(0).text());
            Assert.assertEquals(CaElement.class, wordElements.get(1).getClass());
            Assert.assertEquals(eleType.toString(), wordElements.get(1).text());
            Assert.assertEquals(OrthoWordText.class, wordElements.get(2).getClass());
            Assert.assertEquals("rd", wordElements.get(2).text());
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
            Assert.assertEquals(delimType.toString(), wordElements.get(0).text());
            Assert.assertEquals(OrthoWordText.class, wordElements.get(1).getClass());
            Assert.assertEquals("word", wordElements.get(1).text());
            Assert.assertEquals(CaDelimiter.class, wordElements.get(2).getClass());
            Assert.assertEquals(delimType.toString(), wordElements.get(2).text());
        }
    }

    @Test
    public void testDrawl() {
        final String drawlText = "du:";
        final Orthography ortho = roundTrip(drawlText);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("du", ((OrthoWord)ortho.elementAt(0)).getWord());
        final List<OrthoWordElement> wordElements = ((OrthoWord)ortho.elementAt(0)).getWordElements();
        Assert.assertEquals(2, wordElements.size());
        Assert.assertEquals(OrthoWordText.class, wordElements.get(0).getClass());
        Assert.assertEquals("du", wordElements.get(0).text());
        Assert.assertEquals(Prosody.class, wordElements.get(1).getClass());
        Assert.assertEquals(ProsodyType.DRAWL, ((Prosody)wordElements.get(1)).getType());
    }

    @Test
    public void testPause() {
        final String text = "wo^rd";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("word", ((OrthoWord)ortho.elementAt(0)).getWord());
        final List<OrthoWordElement> wordElements = ((OrthoWord)ortho.elementAt(0)).getWordElements();
        Assert.assertEquals(3, wordElements.size());
        Assert.assertEquals(OrthoWordText.class, wordElements.get(0).getClass());
        Assert.assertEquals("wo", wordElements.get(0).text());
        Assert.assertEquals(Prosody.class, wordElements.get(1).getClass());
        Assert.assertEquals(ProsodyType.PAUSE, ((Prosody)wordElements.get(1)).getType());
        Assert.assertEquals(OrthoWordText.class, wordElements.get(2).getClass());
        Assert.assertEquals("rd", wordElements.get(2).text());
    }

    @Test
    public void testBlocking() {
        final String text = "^word";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("word", ((OrthoWord)ortho.elementAt(0)).getWord());
        final List<OrthoWordElement> wordElements = ((OrthoWord)ortho.elementAt(0)).getWordElements();
        Assert.assertEquals(2, wordElements.size());
        Assert.assertEquals(Prosody.class, wordElements.get(0).getClass());
        Assert.assertEquals(ProsodyType.BLOCKING, ((Prosody)wordElements.get(0)).getType());
        Assert.assertEquals(OrthoWordText.class, wordElements.get(1).getClass());
        Assert.assertEquals("word", wordElements.get(1).text());
    }

    @Test
    public void testShortening() {
        final String text = "(w)ord wo(r)d wo(rd)";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(3, ortho.length());
        // beginning of word
        Assert.assertEquals(OrthoWord.class, ortho.elementAt(0).getClass());
        final OrthoWord word1 = (OrthoWord) ortho.elementAt(0);
        Assert.assertEquals(2, word1.getWordElements().size());
        Assert.assertEquals(Shortening.class, word1.getWordElements().get(0).getClass());
        Assert.assertEquals("w", ((Shortening)word1.getWordElements().get(0)).getOrthoText().text());
        Assert.assertEquals(OrthoWordText.class, word1.getWordElements().get(1).getClass());
        Assert.assertEquals("ord", ((OrthoWordText)word1.getWordElements().get(1)).text());
        // middle of word
        Assert.assertEquals(OrthoWord.class, ortho.elementAt(1).getClass());
        final OrthoWord word2 = (OrthoWord) ortho.elementAt(1);
        Assert.assertEquals(3, word2.getWordElements().size());
        Assert.assertEquals(OrthoWordText.class, word2.getWordElements().get(0).getClass());
        Assert.assertEquals("wo", ((OrthoWordText)word2.getWordElements().get(0)).text());
        Assert.assertEquals(Shortening.class, word2.getWordElements().get(1).getClass());
        Assert.assertEquals("r", ((Shortening)word2.getWordElements().get(1)).getOrthoText().text());
        Assert.assertEquals(OrthoWordText.class, word2.getWordElements().get(2).getClass());
        Assert.assertEquals("d", ((OrthoWordText)word2.getWordElements().get(2)).text());
        // end of word
        Assert.assertEquals(OrthoWord.class, ortho.elementAt(2).getClass());
        final OrthoWord word3 = (OrthoWord) ortho.elementAt(2);
        Assert.assertEquals(2, word3.getWordElements().size());
        Assert.assertEquals(OrthoWordText.class, word3.getWordElements().get(0).getClass());
        Assert.assertEquals("wo", ((OrthoWordText)word3.getWordElements().get(0)).text());
        Assert.assertEquals(Shortening.class, word3.getWordElements().get(1).getClass());
        Assert.assertEquals("rd", ((Shortening)word3.getWordElements().get(1)).getOrthoText().text());
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
