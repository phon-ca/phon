package ca.phon.orthography;

import ca.phon.orthography.parser.UnicodeOrthographyLexer;
import ca.phon.orthography.parser.UnicodeOrthographyBuilder;
import ca.phon.orthography.parser.UnicodeOrthographyParser;
import ca.phon.util.Language;
import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser tests for UnicodeOrthography.g4 grammar
 *
 */
@RunWith(JUnit4.class)
public class TestUnicodeOrthographyParser {

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
            Assert.assertEquals(Word.class, ortho.elementAt(1).getClass());
            Assert.assertEquals("word", ortho.elementAt(1).toString());
        }
    }

    @Test
    public void testWord() {
        final String text = "word";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
    }

    @Test
    public void testCompoundWord() {
        for(CompoundWordMarkerType type: CompoundWordMarkerType.values()) {
            final String text = "hello" + type.getMarker() + "world";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(CompoundWord.class, ortho.elementAt(0).getClass());
            final CompoundWord compoundWord = (CompoundWord) ortho.elementAt(0);
            Assert.assertEquals("hello", compoundWord.getWord1().toString());
            Assert.assertEquals("world", compoundWord.getWord2().toString());
            Assert.assertEquals(type, compoundWord.getMarker().getType());
        }
    }

    @Test
    public void testWordPos() {
        final String text = "hello world$n:t:p";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(2, ortho.length());
        Assert.assertEquals(Word.class, ortho.elementAt(1).getClass());
        final Word w2 = (Word) ortho.elementAt(1);
        Assert.assertEquals("world", w2.getWord());
        Assert.assertEquals(1, w2.getSuffix().getPos().size());
        Assert.assertEquals("n", w2.getSuffix().getPos().get(0).getCategory());
        Assert.assertEquals(2, w2.getSuffix().getPos().get(0).getSubCategories().size());
        Assert.assertEquals("t", w2.getSuffix().getPos().get(0).getSubCategories().get(0));
        Assert.assertEquals("p", w2.getSuffix().getPos().get(0).getSubCategories().get(1));
    }

    @Test
    public void testWordLangs() {
        final String[] texts = {
                "hello world",
                "hello world@s",
                "hello world@s:eng",
                "hello world@s:eng+fra",
                "hello world@s:eng&fra&deu"
        };
        final Langs.LangsType[] types = {
                Langs.LangsType.UNSPECIFIED,
                Langs.LangsType.SECONDARY,
                Langs.LangsType.SINGLE,
                Langs.LangsType.MULTIPLE,
                Langs.LangsType.AMBIGUOUS
        };
        final String[] langs = {
                "", "", "eng", "eng+fra", "eng&fra&deu"
        };
        for(int i = 0; i < texts.length; i++) {
            final String text = texts[i];
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(2, ortho.length());
            Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
            final Word w2 = (Word) ortho.elementAt(1);
            Assert.assertEquals(types[i], w2.getLangs().getType());
            final String delim = switch(types[i]) {
                case UNSPECIFIED, SECONDARY, SINGLE -> "";
                case MULTIPLE -> "+";
                case AMBIGUOUS -> "&";
            };
            final String langText = w2.getLangs().getLangs().stream().map(l -> l.toString()).collect(Collectors.joining(delim));
            Assert.assertEquals(langs[i], langText);
        }
    }

    @Test
    public void testWordLangsWithPos() {
        final String[] texts = {
                "hello world$n",
                "hello world@s$n",
                "hello world@s:eng$n",
                "hello world@s:eng+fra$n",
                "hello world@s:eng&fra&deu$n"
        };
        final Langs.LangsType[] types = {
                Langs.LangsType.UNSPECIFIED,
                Langs.LangsType.SECONDARY,
                Langs.LangsType.SINGLE,
                Langs.LangsType.MULTIPLE,
                Langs.LangsType.AMBIGUOUS
        };
        final String[] langs = {
                "", "", "eng", "eng+fra", "eng&fra&deu"
        };
        for(int i = 0; i < texts.length; i++) {
            final String text = texts[i];
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(2, ortho.length());
            Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
            final Word w2 = (Word) ortho.elementAt(1);
            Assert.assertEquals(types[i], w2.getLangs().getType());
            final String delim = switch(types[i]) {
                case UNSPECIFIED, SECONDARY, SINGLE -> "";
                case MULTIPLE -> "+";
                case AMBIGUOUS -> "&";
            };
            final String langText = w2.getLangs().getLangs().stream().map(l -> l.toString()).collect(Collectors.joining(delim));
            Assert.assertEquals(langs[i], langText);
            Assert.assertEquals(1, w2.getSuffix().getPos().size());
            Assert.assertEquals("n", w2.getSuffix().getPos().get(0).getCategory());
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
            Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
            Assert.assertEquals(OverlapPoint.class, ((Word)ortho.elementAt(0)).getWordElements().get(1).getClass());
            Assert.assertEquals(Word.class, ortho.elementAt(3).getClass());
            Assert.assertEquals(OverlapPoint.class, ((Word)ortho.elementAt(3)).getWordElements().get(0).getClass());
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
            Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
            Assert.assertEquals(OverlapPoint.class, ((Word)ortho.elementAt(0)).getWordElements().get(1).getClass());
            Assert.assertEquals(i+1, ((OverlapPoint)((Word)ortho.elementAt(0)).getWordElements().get(1)).getIndex());
            Assert.assertEquals(Word.class, ortho.elementAt(3).getClass());
            Assert.assertEquals(OverlapPoint.class, ((Word)ortho.elementAt(3)).getWordElements().get(0).getClass());
            Assert.assertEquals(i+1, ((OverlapPoint)((Word)ortho.elementAt(3)).getWordElements().get(0)).getIndex());
        }
    }

    @Test
    public void testCaElements() {
        for(CaElementType eleType:CaElementType.values()) {
            final String text = "wo" + eleType.toString() + "rd";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
            Assert.assertEquals("word", ((Word)ortho.elementAt(0)).getWord());
            final List<WordElement> wordElements = ((Word)ortho.elementAt(0)).getWordElements();
            Assert.assertEquals(3, wordElements.size());
            Assert.assertEquals(WordText.class, wordElements.get(0).getClass());
            Assert.assertEquals("wo", wordElements.get(0).text());
            Assert.assertEquals(CaElement.class, wordElements.get(1).getClass());
            Assert.assertEquals(eleType.toString(), wordElements.get(1).text());
            Assert.assertEquals(WordText.class, wordElements.get(2).getClass());
            Assert.assertEquals("rd", wordElements.get(2).text());
        }
    }

    @Test
    public void testCaDelimiters() {
        for(CaDelimiterType delimType:CaDelimiterType.values()) {
            final String text = delimType.toString() + "word" + delimType.toString();
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
            Assert.assertEquals("word", ((Word)ortho.elementAt(0)).getWord());
            final List<WordElement> wordElements = ((Word)ortho.elementAt(0)).getWordElements();
            Assert.assertEquals(3, wordElements.size());
            Assert.assertEquals(CaDelimiter.class, wordElements.get(0).getClass());
            final CaDelimiter cd1 = (CaDelimiter) wordElements.get(0);
            Assert.assertEquals(BeginEnd.BEGIN, cd1.getBeginEnd());
            Assert.assertEquals(delimType, cd1.getType());
            Assert.assertEquals(WordText.class, wordElements.get(1).getClass());
            Assert.assertEquals("word", wordElements.get(1).text());
            Assert.assertEquals(CaDelimiter.class, wordElements.get(2).getClass());
            final CaDelimiter cd2 = (CaDelimiter) wordElements.get(2);
            Assert.assertEquals(BeginEnd.END, cd2.getBeginEnd());
            Assert.assertEquals(delimType, cd2.getType());
        }
    }

    @Test
    public void testDrawl() {
        final String drawlText = "du:";
        final Orthography ortho = roundTrip(drawlText);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("du", ((Word)ortho.elementAt(0)).getWord());
        final List<WordElement> wordElements = ((Word)ortho.elementAt(0)).getWordElements();
        Assert.assertEquals(2, wordElements.size());
        Assert.assertEquals(WordText.class, wordElements.get(0).getClass());
        Assert.assertEquals("du", wordElements.get(0).text());
        Assert.assertEquals(Prosody.class, wordElements.get(1).getClass());
        Assert.assertEquals(ProsodyType.DRAWL, ((Prosody)wordElements.get(1)).getType());
    }

    @Test
    public void testPause() {
        final String text = "wo^rd";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("word", ((Word)ortho.elementAt(0)).getWord());
        final List<WordElement> wordElements = ((Word)ortho.elementAt(0)).getWordElements();
        Assert.assertEquals(3, wordElements.size());
        Assert.assertEquals(WordText.class, wordElements.get(0).getClass());
        Assert.assertEquals("wo", wordElements.get(0).text());
        Assert.assertEquals(Prosody.class, wordElements.get(1).getClass());
        Assert.assertEquals(ProsodyType.PAUSE, ((Prosody)wordElements.get(1)).getType());
        Assert.assertEquals(WordText.class, wordElements.get(2).getClass());
        Assert.assertEquals("rd", wordElements.get(2).text());
    }

    @Test
    public void testBlocking() {
        final String text = "^word";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("word", ((Word)ortho.elementAt(0)).getWord());
        final List<WordElement> wordElements = ((Word)ortho.elementAt(0)).getWordElements();
        Assert.assertEquals(2, wordElements.size());
        Assert.assertEquals(Prosody.class, wordElements.get(0).getClass());
        Assert.assertEquals(ProsodyType.BLOCKING, ((Prosody)wordElements.get(0)).getType());
        Assert.assertEquals(WordText.class, wordElements.get(1).getClass());
        Assert.assertEquals("word", wordElements.get(1).text());
    }

    @Test
    public void testShortening() {
        final String text = "(w)ord wo(r)d wo(rd)";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(3, ortho.length());
        // beginning of word
        Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
        final Word word1 = (Word) ortho.elementAt(0);
        Assert.assertEquals(2, word1.getWordElements().size());
        Assert.assertEquals(Shortening.class, word1.getWordElements().get(0).getClass());
        Assert.assertEquals("w", ((Shortening)word1.getWordElements().get(0)).getOrthoText().text());
        Assert.assertEquals(WordText.class, word1.getWordElements().get(1).getClass());
        Assert.assertEquals("ord", ((WordText)word1.getWordElements().get(1)).text());
        // middle of word
        Assert.assertEquals(Word.class, ortho.elementAt(1).getClass());
        final Word word2 = (Word) ortho.elementAt(1);
        Assert.assertEquals(3, word2.getWordElements().size());
        Assert.assertEquals(WordText.class, word2.getWordElements().get(0).getClass());
        Assert.assertEquals("wo", ((WordText)word2.getWordElements().get(0)).text());
        Assert.assertEquals(Shortening.class, word2.getWordElements().get(1).getClass());
        Assert.assertEquals("r", ((Shortening)word2.getWordElements().get(1)).getOrthoText().text());
        Assert.assertEquals(WordText.class, word2.getWordElements().get(2).getClass());
        Assert.assertEquals("d", ((WordText)word2.getWordElements().get(2)).text());
        // end of word
        Assert.assertEquals(Word.class, ortho.elementAt(2).getClass());
        final Word word3 = (Word) ortho.elementAt(2);
        Assert.assertEquals(2, word3.getWordElements().size());
        Assert.assertEquals(WordText.class, word3.getWordElements().get(0).getClass());
        Assert.assertEquals("wo", ((WordText)word3.getWordElements().get(0)).text());
        Assert.assertEquals(Shortening.class, word3.getWordElements().get(1).getClass());
        Assert.assertEquals("rd", ((Shortening)word3.getWordElements().get(1)).getOrthoText().text());
    }

    @Test
    public void testTagMarkers() {
        for(TagMarkerType tmType:TagMarkerType.values()) {
            final String text = "wo " + tmType.getChar() + " rd";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(3, ortho.length());
            Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
            Assert.assertEquals("wo", ortho.elementAt(0).toString());
            Assert.assertEquals(TagMarker.class, ortho.elementAt(1).getClass());
            Assert.assertEquals(tmType.getChar()+"", ortho.elementAt(1).toString());
            Assert.assertEquals(Word.class, ortho.elementAt(2).getClass());
            Assert.assertEquals("rd", ortho.elementAt(2).toString());
        }
    }

    @Test
    public void testTerminators() {
        for(TerminatorType tt:TerminatorType.values()) {
            final String text = "word " + tt.toString();
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(2, ortho.length());
            Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
            Assert.assertEquals("word", ortho.elementAt(0).toString());
            Assert.assertEquals(Terminator.class, ortho.elementAt(1).getClass());
            Assert.assertEquals(tt.toString(), ortho.elementAt(1).toString());
        }
    }

    @Test
    public void testSymbolicPause() {
        final String text = "one (.) two (..) three (...)";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(6, ortho.length());
        Assert.assertEquals(Pause.class, ortho.elementAt(1).getClass());
        Assert.assertEquals(PauseLength.SIMPLE, ((Pause)ortho.elementAt(1)).getType());
        Assert.assertEquals(Pause.class, ortho.elementAt(3).getClass());
        Assert.assertEquals(PauseLength.LONG, ((Pause)ortho.elementAt(3)).getType());
        Assert.assertEquals(Pause.class, ortho.elementAt(5).getClass());
        Assert.assertEquals(PauseLength.VERY_LONG, ((Pause)ortho.elementAt(5)).getType());
    }

    @Test
    public void testNumericPause() {
        final String text = "one (1.) two (2.15) three (1:05.2)";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(6, ortho.length());
        Assert.assertEquals(Pause.class, ortho.elementAt(1).getClass());
        Assert.assertEquals(PauseLength.NUMERIC, ((Pause)ortho.elementAt(1)).getType());
        Assert.assertEquals(Pause.class, ortho.elementAt(3).getClass());
        Assert.assertEquals(PauseLength.NUMERIC, ((Pause)ortho.elementAt(3)).getType());
        Assert.assertEquals(Pause.class, ortho.elementAt(5).getClass());
        Assert.assertEquals(PauseLength.NUMERIC, ((Pause)ortho.elementAt(5)).getType());
    }

    @Test
    public void testFreecode() {
        final String text = "hello [^ foo] world";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(3, ortho.length());
        Assert.assertEquals(Freecode.class, ortho.elementAt(1).getClass());
        Assert.assertEquals("foo", ((Freecode)ortho.elementAt(1)).getCode().trim());
    }

    @Test
    public void testInternalMedia() {
        final String text = "hello \u20220.5-1.2\u2022 world \u20221:05.1-1:06.\u2022";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(4, ortho.length());
        Assert.assertEquals(InternalMedia.class, ortho.elementAt(1).getClass());
        final InternalMedia media1 = (InternalMedia)ortho.elementAt(1);
        Assert.assertEquals(0.5f, media1.getStartTime(), 0.0001);
        Assert.assertEquals(1.2f, media1.getEndTime(), 0.0001);
        Assert.assertEquals(InternalMedia.class, ortho.elementAt(3).getClass());
        final InternalMedia media2 = (InternalMedia)ortho.elementAt(3);
        Assert.assertEquals(65.1f, media2.getStartTime(), 0.0001);
        Assert.assertEquals(66.0f, media2.getEndTime(), 0.0001);
    }

    @Test
    public void testSeparator() {
        for(SeparatorType type:SeparatorType.values()) {
            final String text = "hello " + type.getText() + " world";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(3, ortho.length());
            Assert.assertEquals(Separator.class, ortho.elementAt(1).getClass());
            Assert.assertEquals(type, ((Separator)ortho.elementAt(1)).getType());
        }
    }

    @Test
    public void testToneMarker() {
        for(ToneMarkerType type:ToneMarkerType.values()) {
            final String text = "hello " + type.toString() + " world";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(3, ortho.length());
            Assert.assertEquals(ToneMarker.class, ortho.elementAt(1).getClass());
            Assert.assertEquals(type, ((ToneMarker)ortho.elementAt(1)).getType());
        }
    }

    @Test
    public void testAction() {
        final String text = "0";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Action.class, ortho.elementAt(0).getClass());
    }

    @Test
    public void testHappening() {
        final String text = Happening.PREFIX + "foo";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Happening.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("foo", ((Happening)ortho.elementAt(0)).getData());
    }

    @Test
    public void testOtherSpokenEvent() {
        final String text = OtherSpokenEvent.PREFIX + "CHI=foo";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(OtherSpokenEvent.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("CHI", ((OtherSpokenEvent)ortho.elementAt(0)).getWho());
        Assert.assertEquals("foo", ((OtherSpokenEvent)ortho.elementAt(0)).getData());
    }

    @Test
    public void testMarker() {
        for(MarkerType type:MarkerType.values()) {
            final String text = Happening.PREFIX + "foo " + type.getText();
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(Happening.class, ortho.elementAt(0).getClass());
            final Happening happening = (Happening) ortho.elementAt(0);
            Assert.assertEquals(1, happening.getAnnotations().size());
            Assert.assertEquals(Marker.class, happening.getAnnotations().get(0).getClass());
            final Marker maker = (Marker) happening.getAnnotations().get(0);
            Assert.assertEquals(type, maker.getType());
        }
    }

    @Test
    public void testError() {
        final String text = Happening.PREFIX + "foo " + Error.PREFIX + " bar]";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Happening.class, ortho.elementAt(0).getClass());
        final Happening happening = (Happening) ortho.elementAt(0);
        Assert.assertEquals(1, happening.getAnnotations().size());
        Assert.assertEquals(Error.class, happening.getAnnotations().get(0).getClass());
        final Error error = (Error) happening.getAnnotations().get(0);
        Assert.assertEquals("bar", error.getData().trim());
    }

    @Test
    public void testOverlap() {
        for(OverlapType type:OverlapType.values()) {
            final String text = Happening.PREFIX + "foo " + type.getPrefix() + "]";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(Happening.class, ortho.elementAt(0).getClass());
            final Happening happening = (Happening) ortho.elementAt(0);
            Assert.assertEquals(1, happening.getAnnotations().size());
            Assert.assertEquals(Overlap.class, happening.getAnnotations().get(0).getClass());
            final Overlap overlap = (Overlap) happening.getAnnotations().get(0);
            Assert.assertEquals(type, overlap.getType());
        }
    }

    @Test
    public void testIndexedOverlap() {
        int idx = 1;
        for(OverlapType type:OverlapType.values()) {
            final String text = Happening.PREFIX + "foo " + type.getPrefix() + (idx) + "]";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(Happening.class, ortho.elementAt(0).getClass());
            final Happening happening = (Happening) ortho.elementAt(0);
            Assert.assertEquals(1, happening.getAnnotations().size());
            Assert.assertEquals(Overlap.class, happening.getAnnotations().get(0).getClass());
            final Overlap overlap = (Overlap) happening.getAnnotations().get(0);
            Assert.assertEquals(type, overlap.getType());
            Assert.assertEquals(idx, overlap.getIndex());
            ++idx;
        }
    }

    @Test
    public void testGroupAnnotation() {
        for(GroupAnnotationType type:GroupAnnotationType.values()) {
            final String text = Happening.PREFIX + "foo [" + type.getPrefix() + " bar]";
            final Orthography ortho = roundTrip(text);
            Assert.assertEquals(1, ortho.length());
            Assert.assertEquals(Happening.class, ortho.elementAt(0).getClass());
            final Happening happening = (Happening) ortho.elementAt(0);
            Assert.assertEquals(1, happening.getAnnotations().size());
            Assert.assertEquals(GroupAnnotation.class, happening.getAnnotations().get(0).getClass());
            final GroupAnnotation groupAnnotation = (GroupAnnotation) happening.getAnnotations().get(0);
            Assert.assertEquals(type, groupAnnotation.getType());
            Assert.assertEquals("bar", groupAnnotation.getData().trim());
        }
    }

    @Test
    public void testDuration() {
        final String text = Happening.PREFIX + "foo [# 1.2]";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Happening.class, ortho.elementAt(0).getClass());
        final Happening happening = (Happening) ortho.elementAt(0);
        Assert.assertEquals(1, happening.getAnnotations().size());
        Assert.assertEquals(Duration.class, happening.getAnnotations().get(0).getClass());
        final Duration duration = (Duration) happening.getAnnotations().get(0);
        Assert.assertEquals(1.2f, duration.getDuration(), 0.0001f);
    }

    @Test
    public void testSimpleGroup() {
        final String text = "foo [# 1.2]";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(OrthoGroup.class, ortho.elementAt(0).getClass());
        final OrthoGroup group = (OrthoGroup) ortho.elementAt(0);
        Assert.assertEquals(1, group.getElements().size());
        Assert.assertEquals(Word.class, group.getElements().get(0).getClass());
        Assert.assertEquals("foo", ((Word)group.getElements().get(0)).getWord());
        Assert.assertEquals(1, group.getAnnotations().size());
        Assert.assertEquals(Duration.class, group.getAnnotations().get(0).getClass());
        final Duration duration = (Duration) group.getAnnotations().get(0);
        Assert.assertEquals(1.2f, duration.getDuration(), 0.0001f);
    }

    @Test
    public void testExplicitGroup() {
        final String text = "<foo bar> [# 1.2]";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(OrthoGroup.class, ortho.elementAt(0).getClass());
        final OrthoGroup group = (OrthoGroup) ortho.elementAt(0);
        Assert.assertEquals(2, group.getElements().size());
        Assert.assertEquals(Word.class, group.getElements().get(0).getClass());
        Assert.assertEquals("foo", ((Word)group.getElements().get(0)).getWord());
        Assert.assertEquals(Word.class, group.getElements().get(1).getClass());
        Assert.assertEquals("bar", ((Word)group.getElements().get(1)).getWord());
        Assert.assertEquals(1, group.getAnnotations().size());
        Assert.assertEquals(Duration.class, group.getAnnotations().get(0).getClass());
        final Duration duration = (Duration) group.getAnnotations().get(0);
        Assert.assertEquals(1.2f, duration.getDuration(), 0.0001f);
    }

    @Test
    public void testPhoneticGroup() {
        final String text = PhoneticGroup.PHONETIC_GROUP_START + "foo bar" + PhoneticGroup.PHONETIC_GROUP_END;
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(PhoneticGroup.class, ortho.elementAt(0).getClass());
        final PhoneticGroup group = (PhoneticGroup) ortho.elementAt(0);
        Assert.assertEquals(2, group.getElements().size());
        Assert.assertEquals(Word.class, group.getElements().get(0).getClass());
        Assert.assertEquals("foo", ((Word)group.getElements().get(0)).getWord());
        Assert.assertEquals(Word.class, group.getElements().get(1).getClass());
        Assert.assertEquals("bar", ((Word)group.getElements().get(1)).getWord());
    }

    @Test
    public void testLongFeature() {
        final String text = "hello " + LongFeature.LONG_FEATURE_START + "laughs world " +
                LongFeature.LONG_FEATURE_END + "laughs .";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(5, ortho.length());
        Assert.assertEquals(LongFeature.class, ortho.elementAt(1).getClass());
        final LongFeature lf1 = (LongFeature) ortho.elementAt(1);
        Assert.assertEquals(BeginEnd.BEGIN, lf1.getBeginEnd());
        Assert.assertEquals("laughs", lf1.getLabel());
        Assert.assertEquals(LongFeature.class, ortho.elementAt(3).getClass());
        final LongFeature lf2 = (LongFeature) ortho.elementAt(3);
        Assert.assertEquals(BeginEnd.END, lf2.getBeginEnd());
        Assert.assertEquals("laughs", lf2.getLabel());
    }

    @Test
    public void testNonvocal() {
        final String text = "is &{n=THUMP_MICROPHONE &=nonvocal this &=nonvocal &{l=X one &}l=X working &}n=THUMP_MICROPHONE &}l=PAR &{n=PUNCH}";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(12, ortho.length());
        Assert.assertEquals(Nonvocal.class, ortho.elementAt(1).getClass());
        final Nonvocal nv1 = (Nonvocal) ortho.elementAt(1);
        Assert.assertEquals(BeginEndSimple.BEGIN, nv1.getBeginEndSimple());
        Assert.assertEquals("THUMP_MICROPHONE", nv1.getLabel());
        Assert.assertEquals(Nonvocal.class, ortho.elementAt(9).getClass());
        final Nonvocal nv2 = (Nonvocal) ortho.elementAt(9);
        Assert.assertEquals(BeginEndSimple.END, nv2.getBeginEndSimple());
        Assert.assertEquals("THUMP_MICROPHONE", nv2.getLabel());
        Assert.assertEquals(Nonvocal.class, ortho.elementAt(11).getClass());
        final Nonvocal nv3 = (Nonvocal) ortho.elementAt(11);
        Assert.assertEquals(BeginEndSimple.SIMPLE, nv3.getBeginEndSimple());
        Assert.assertEquals("PUNCH", nv3.getLabel());
    }

    @Test
    public void testPostcode() {
        final String text = "<ryoote agete> [=! singing] . [+ bch] [+ foo]";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(4, ortho.length());
        Assert.assertEquals(Postcode.class, ortho.elementAt(2).getClass());
        final Postcode pc1 = (Postcode) ortho.elementAt(2);
        Assert.assertEquals("bch", pc1.getCode());
        Assert.assertEquals(Postcode.class, ortho.elementAt(3).getClass());
        final Postcode pc2 = (Postcode) ortho.elementAt(3);
        Assert.assertEquals("foo", pc2.getCode());
    }

    @Test
    public void testQuotation() {
        final String text = Quotation.QUOTATION_BEGIN + " hello world " + Quotation.QUOTATION_END;
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(4, ortho.length());
        Assert.assertEquals(Quotation.class, ortho.elementAt(0).getClass());
        final Quotation q1 = (Quotation) ortho.elementAt(0);
        Assert.assertEquals(BeginEnd.BEGIN, q1.getBeginEnd());
        Assert.assertEquals(Quotation.class, ortho.elementAt(3).getClass());
        final Quotation q2 = (Quotation) ortho.elementAt(3);
        Assert.assertEquals(BeginEnd.END, q2.getBeginEnd());
    }

    @Test
    public void testReplacement() {
        final String text = "hello [: foo] world [:: bar] [: foo bar]";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(2, ortho.length());
        Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
        final Word w1 = (Word) ortho.elementAt(0);
        Assert.assertEquals(1, w1.getReplacements().size());
        final Replacement r1 = w1.getReplacements().get(0);
        Assert.assertEquals(false, r1.isReal());
        Assert.assertEquals("foo", r1.getWordText());
        Assert.assertEquals(Word.class, ortho.elementAt(1).getClass());
        final Word w2 = (Word) ortho.elementAt(1);
        Assert.assertEquals(2, w2.getReplacements().size());
        final Replacement r2 = w2.getReplacements().get(0);
        Assert.assertEquals(true, r2.isReal());
        Assert.assertEquals("bar", r2.getWordText());
        final Replacement r3 = w2.getReplacements().get(1);
        Assert.assertEquals(false, r3.isReal());
        Assert.assertEquals("foo bar", r3.getWordText());
    }

    @Test
    public void testUserSpecialForm() {
        final String text = "hello@z:rtfd";
        final Orthography ortho = roundTrip(text);
        Assert.assertEquals(1, ortho.length());
        Assert.assertEquals(Word.class, ortho.elementAt(0).getClass());
        Assert.assertEquals("rtfd", ((Word)ortho.elementAt(0)).getSuffix().getUserSpecialForm());
    }

}
