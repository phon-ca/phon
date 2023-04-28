package ca.phon.orthography.parser;

import ca.phon.orthography.*;
import ca.phon.orthography.parser.exceptions.OrthoParserException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public final class UnicodeOrthographyBuilder extends AbstractUnicodeOrthographyParserListener {

    private final OrthographyBuilder builder = new OrthographyBuilder();

    private List<WordElement> wordElements = new ArrayList<>();

    @Override
    public void exitLinker(UnicodeOrthographyParser.LinkerContext ctx) {
        final LinkerType lt = LinkerType.fromString(ctx.getText());
        builder.append(new Linker(lt));
    }

    @Override
    public void exitComplete_word(UnicodeOrthographyParser.Complete_wordContext ctx) {
        if(builder.size() == 0) return;
        if(builder.lastElement() instanceof Word) { // should always be true
            Word lastWord = (Word) builder.lastElement();
            List<WordElement> wordElements = lastWord.getWordElements();

            // handle special cases
            // overlap-point
            if(wordElements.size() == 1 && wordElements.get(0) instanceof OverlapPoint) {
                builder.replaceLastElement((OverlapPoint)wordElements.get(0));
            } else {
                WordType wordType = null;
                WordFormType formType = null;
                String pos = null;
                UntranscribedType untranscribedType = null;
                if (ctx.wordprefix() != null) {
                    wordType = WordType.fromCode(ctx.wordprefix().getText());
                    if (wordType == null)
                        throw new OrthoParserException("Invalid word prefix '" + ctx.wordprefix().getText() + "'",
                                ctx.wordprefix().getStart().getCharPositionInLine());
                }
                if (ctx.wordsuffix() != null) {
                    if (ctx.wordsuffix().formtype() != null) {
                        formType = WordFormType.fromCode(ctx.wordsuffix().formtype().getText());
                    }
                    if (ctx.wordsuffix().wordpos() != null) {
                        pos = ctx.wordsuffix().wordpos().getText();
                    }
                }
                if (wordElements.size() == 1 && wordElements.get(0).text().equals("xxx")) {
                    untranscribedType = UntranscribedType.UNINTELLIGIBLE;
                } else if (wordElements.size() == 1 && wordElements.get(0).text().equals("yyy")) {
                    untranscribedType = UntranscribedType.UNINTELLIGIBLE_WORD_WITH_PHO;
                } else if (wordElements.size() == 1 && wordElements.get(0).text().equals("www")) {
                    untranscribedType = UntranscribedType.UNTRANSCRIBED;
                }
                WordPrefix prefix = (wordType == null ? null : new WordPrefix(wordType));
                WordSuffix suffix = (formType != null || pos != null ? new WordSuffix(formType, null, null, pos) : null);

                builder.annnotateWord(prefix, suffix, untranscribedType);
            }
        }
    }

    @Override
    public void exitSingleWord(UnicodeOrthographyParser.SingleWordContext ctx) {
        builder.appendWord(wordElements);
        wordElements.clear();
    }

    @Override
    public void exitCompoundWord(UnicodeOrthographyParser.CompoundWordContext ctx) {
        CompoundWordMarkerType type = CompoundWordMarkerType.fromMarker(ctx.wk().getText().charAt(0));
        builder.createCompoundWord(type);
    }

    @Override
    public void exitCa_element(UnicodeOrthographyParser.Ca_elementContext ctx) {
        final CaElementType eleType = CaElementType.fromString(ctx.getText());
        if(eleType != null) {
            wordElements.add(new CaElement(eleType));
        } else {
            throw new IllegalArgumentException(ctx.getText());
        }
    }

    @Override
    public void exitCa_delimiter(UnicodeOrthographyParser.Ca_delimiterContext ctx) {
        final CaDelimiterType eleType = CaDelimiterType.fromString(ctx.getText());
        if(eleType != null) {
            wordElements.add(new CaDelimiter(eleType));
        } else {
            throw new OrthoParserException(ctx.getText(), ctx.getStart().getCharPositionInLine());
        }
    }

    @Override
    public void exitText(UnicodeOrthographyParser.TextContext ctx) {
        wordElements.add(new WordText(ctx.getText()));
    }

    @Override
    public void exitShortening(UnicodeOrthographyParser.ShorteningContext ctx) {
        // text has been added as a word element
        if(wordElements.size() == 0)
            throw new OrthoParserException("Shortening must include text", ctx.getStart().getCharPositionInLine());
        if(!(wordElements.get(wordElements.size()-1) instanceof WordText))
            throw new OrthoParserException("Shortening must only include text data", ctx.getStart().getCharPositionInLine());
        final WordText text = (WordText) wordElements.remove(wordElements.size()-1);
        wordElements.add(new Shortening(text));
    }

    @Override
    public void exitTagMarker(UnicodeOrthographyParser.TagMarkerContext ctx) {
        final TagMarkerType tmType = TagMarkerType.fromChar(ctx.getText().charAt(0));
        builder.append(new TagMarker(tmType));
    }

    @Override
    public void exitProsody(UnicodeOrthographyParser.ProsodyContext ctx) {
        ProsodyType type = switch(ctx.getText()) {
            case ":" -> ProsodyType.DRAWL;
            case "^" -> (wordElements.size() == 0 ? ProsodyType.BLOCKING : ProsodyType.PAUSE);
            default -> null;
        };
        if(type != null) {
            wordElements.add(new Prosody(type));
        } else {
            throw new IllegalArgumentException(ctx.getText());
        }
    }

    @Override
    public void exitOverlap_point(UnicodeOrthographyParser.Overlap_pointContext ctx) {
        final OverlapPointType type = OverlapPointType.fromString(ctx.OVERLAP_POINT().getText());
        final int index = (ctx.digit() != null ? Integer.parseInt(ctx.digit().getText()) : -1);
        wordElements.add(new OverlapPoint(type, index));
    }

    @Override
    public void exitTerminator(UnicodeOrthographyParser.TerminatorContext ctx) {
        final TerminatorType tt = TerminatorType.fromString(ctx.getText());
        builder.append(new Terminator(tt));
    }

    public Orthography getOrthography() {
        return builder.toOrthography();
    }

    @Override
    public void exitSymbolic_pause(UnicodeOrthographyParser.Symbolic_pauseContext ctx) {
        final PauseLength type = switch (ctx.getText()) {
            case "(.)" -> PauseLength.SIMPLE;
            case "(..)" -> PauseLength.LONG;
            case "(...)" -> PauseLength.VERY_LONG;
            default -> throw new OrthoParserException("Invalid symbolic pause", ctx.getStart().getCharPositionInLine());
        };
        builder.append(new Pause(type));
    }

    @Override
    public void exitNumeric_pause(UnicodeOrthographyParser.Numeric_pauseContext ctx) {
        if(ctx.time_in_minutes_seconds().getText().matches(NumericPauseFormat.PATTERN)) {
            final NumericPauseFormat format = new NumericPauseFormat();
            try {
                Float seconds = (Float) format.parseObject(ctx.time_in_minutes_seconds().getText());
                builder.append(new Pause(PauseLength.NUMERIC, seconds));
            } catch (ParseException pe) {
                throw new OrthoParserException(pe.getMessage(), pe.getErrorOffset());
            }
        } else {
            throw new OrthoParserException("Invalid numeric pause", ctx.getStart().getCharPositionInLine());
        }
    }

    @Override
    public void exitFreecode(UnicodeOrthographyParser.FreecodeContext ctx) {
        final String data = ctx.getText().substring(2, ctx.getText().length()-1);
        builder.append(new Freecode(data));
    }
}
