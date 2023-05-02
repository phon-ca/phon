package ca.phon.orthography.parser;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.parser.exceptions.OrthoParserException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public final class UnicodeOrthographyBuilder extends AbstractUnicodeOrthographyParserListener {

    private OrthographyBuilder builder = new OrthographyBuilder();

    private final Stack<OrthographyBuilder> builderStack = new Stack<>();

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
        // handle some special cases
        if(wordElements.size() == 1) {
            if(wordElements.get(0) instanceof Prosody) {
                // word == ':'
                if(((Prosody)wordElements.get(0)).getType() == ProsodyType.DRAWL) {
                    // switch to separator
                    builder.append(new Separator(SeparatorType.COLON));
                    wordElements.clear();
                    return;
                }
            }
        }
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
        if(ctx.time_in_minutes_seconds().getText().matches(MediaTimeFormat.PATTERN)) {
            final MediaTimeFormat format = new MediaTimeFormat();
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
        builder.append(new Freecode(data.trim()));
    }

    @Override
    public void exitInternal_media(UnicodeOrthographyParser.Internal_mediaContext ctx) {
        final MediaTimeFormat timeFormat = new MediaTimeFormat();
        float startTime = 0.0f;
        try {
            startTime = (float)timeFormat.parseObject(ctx.mediasegment().time_in_minutes_seconds(0).getText());
        } catch(ParseException e) {
            throw new OrthoParserException("Invalid start time", ctx.mediasegment().time_in_minutes_seconds(0).getStart().getCharPositionInLine());
        }
        float endTime = startTime;
        try {
            endTime = (float)timeFormat.parseObject(ctx.mediasegment().time_in_minutes_seconds(1).getText());
        } catch(ParseException e) {
            throw new OrthoParserException("Invalid start time", ctx.mediasegment().time_in_minutes_seconds(1).getStart().getCharPositionInLine());
        }
        builder.append(new InternalMedia(startTime, endTime));
    }

    @Override
    public void exitSeparator(UnicodeOrthographyParser.SeparatorContext ctx) {
        final SeparatorType type = SeparatorType.fromString(ctx.getText());
        if(type != null) {
            builder.append(new Separator(type));
        } else {
            throw new OrthoParserException("Invalid separator", ctx.getStart().getCharPositionInLine());
        }
    }

    @Override
    public void exitToneMarker(UnicodeOrthographyParser.ToneMarkerContext ctx) {
        final ToneMarkerType type = ToneMarkerType.fromString(ctx.getText());
        if(type != null) {
            builder.append(new ToneMarker(type));
        } else {
            throw new OrthoParserException("Invalid tone marker", ctx.getStart().getCharPositionInLine());
        }
    }

    @Override
    public void exitAction(UnicodeOrthographyParser.ActionContext ctx) {
        builder.append(new Action());
    }

    @Override
    public void exitHappening(UnicodeOrthographyParser.HappeningContext ctx) {
        final String text = ctx.id_or_basic_word().getText();
        builder.append(new Happening(text));
    }

    @Override
    public void exitOtherSpokenEvent(UnicodeOrthographyParser.OtherSpokenEventContext ctx) {
        final String who = ctx.id_or_basic_word(0).getText();
        final String text = ctx.id_or_basic_word(1).getText();
        builder.append(new OtherSpokenEvent(who, text));
    }

    private void annotateLastElement(OrthographyAnnotation ele) {
        if(builder.size() > 0) {
            OrthographyElement lastEle = builder.lastElement();
            if(lastEle instanceof AnnotatedOrthographyElement) {
                AnnotatedOrthographyElement annotatedOrthographyElement = (AnnotatedOrthographyElement) lastEle;
                builder.replaceLastElement(annotatedOrthographyElement.cloneAppendingAnnotation(ele));
            } else if(lastEle instanceof Word) {
                Word word = (Word) lastEle;
                builder.replaceLastElement(new OrthoGroup(Collections.singletonList(word), Collections.singletonList(ele)));
            } else {
                builder.append(ele);
            }
        } else {
            builder.append(ele);
        }
    }

    @Override
    public void exitMarker(UnicodeOrthographyParser.MarkerContext ctx) {
        final MarkerType type = MarkerType.fromString(ctx.getText());
        if(type != null) {
            final Marker marker = new Marker(type);
            annotateLastElement(marker);
        } else {
            throw new OrthoParserException("Invalid marker", ctx.getStart().getCharPositionInLine());
        }
    }

    @Override
    public void exitError(UnicodeOrthographyParser.ErrorContext ctx) {
        final Error error = new Error(ctx.getText().substring(2, ctx.getText().length()-1));
        annotateLastElement(error);
    }

    @Override
    public void exitOverlap(UnicodeOrthographyParser.OverlapContext ctx) {
        final OverlapType overlapType = ctx.getText().charAt(1) == '<' ? OverlapType.OVERLAP_PRECEEDS : OverlapType.OVERLAP_FOLLOWS;
        final int index = (ctx.digit() != null ? Integer.parseInt(ctx.digit().getText()) : -1);
        final Overlap overlap = new Overlap(overlapType, index);
        annotateLastElement(overlap);
    }

    private void addGroupAnnotation(GroupAnnotationType type, String data) {
        annotateLastElement(new GroupAnnotation(type, data));
    }

    @Override
    public void exitComment(UnicodeOrthographyParser.CommentContext ctx) {
        final String data = ctx.getText().substring(GroupAnnotationType.COMMENTS.getPrefix().length()+1,
                ctx.getText().length()-1);
        addGroupAnnotation(GroupAnnotationType.COMMENTS, data.trim());
    }

    @Override
    public void exitAlternative(UnicodeOrthographyParser.AlternativeContext ctx) {
        final String data = ctx.getText().substring(GroupAnnotationType.ALTERNATIVE.getPrefix().length()+1,
                ctx.getText().length()-1);
        addGroupAnnotation(GroupAnnotationType.ALTERNATIVE, data.trim());
    }

    @Override
    public void exitExplanation(UnicodeOrthographyParser.ExplanationContext ctx) {
        final String data = ctx.getText().substring(GroupAnnotationType.EXPLANATION.getPrefix().length()+1,
                ctx.getText().length()-1);
        addGroupAnnotation(GroupAnnotationType.EXPLANATION, data.trim());
    }

    @Override
    public void exitParalinguistics(UnicodeOrthographyParser.ParalinguisticsContext ctx) {
        final String data = ctx.getText().substring(GroupAnnotationType.PARALINGUISTICS.getPrefix().length()+1,
                ctx.getText().length()-1);
        addGroupAnnotation(GroupAnnotationType.PARALINGUISTICS, data.trim());
    }

    @Override
    public void exitDuration(UnicodeOrthographyParser.DurationContext ctx) {
        final MediaTimeFormat timeFormat = new MediaTimeFormat();
        float duration = 0.0f;
        try {
            duration = (float) timeFormat.parseObject(ctx.time_in_minutes_seconds().getText());
        } catch (ParseException pe) {
            throw new OrthoParserException(pe.getMessage(), ctx.time_in_minutes_seconds().getStart().getCharPositionInLine());
        }
        annotateLastElement(new Duration(duration));
    }

    @Override
    public void enterGroup(UnicodeOrthographyParser.GroupContext ctx) {
        builderStack.push(builder);
        builder = new OrthographyBuilder();
    }

    @Override
    public void exitGroup(UnicodeOrthographyParser.GroupContext ctx) {
        final Orthography innerOrtho = builder.toOrthography();
        builder = builderStack.pop();
        builder.append(new OrthoGroup(innerOrtho.toList(), new ArrayList<>()));
    }

    @Override
    public void enterPhonetic_group(UnicodeOrthographyParser.Phonetic_groupContext ctx) {
        builderStack.push(builder);
        builder = new OrthographyBuilder();
    }

    @Override
    public void exitPhonetic_group(UnicodeOrthographyParser.Phonetic_groupContext ctx) {
        final Orthography innerOrtho = builder.toOrthography();
        builder = builderStack.pop();
        builder.append(new PhoneticGroup(innerOrtho.toList()));
    }

    @Override
    public void exitLong_feature(UnicodeOrthographyParser.Long_featureContext ctx) {
        final BeginEnd beginEnd =
                ctx.getText().startsWith(LongFeature.LONG_FEATURE_START) ? BeginEnd.BEGIN : BeginEnd.END;
        final String label = ctx.id_or_basic_word().getText();
        builder.append(new LongFeature(beginEnd, label));
    }

    @Override
    public void exitNonvocal(UnicodeOrthographyParser.NonvocalContext ctx) {
        BeginEndSimple beginEndSimple =
                ctx.getText().startsWith(Nonvocal.NONVOCAL_START) ? BeginEndSimple.BEGIN : BeginEndSimple.END;
        if(ctx.CLOSE_BRACE() != null) {
            beginEndSimple = BeginEndSimple.SIMPLE;
        }
        final String label = ctx.id_or_basic_word().getText();
        builder.append(new Nonvocal(beginEndSimple, label));
    }

    @Override
    public void exitPostcode(UnicodeOrthographyParser.PostcodeContext ctx) {
        final String code = ctx.id_or_basic_word().getText();
        builder.append(new Postcode(code));
    }

}