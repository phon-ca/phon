package ca.phon.orthography.parser;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.parser.exceptions.OrthoParserException;
import ca.phon.util.Language;

import java.text.ParseException;
import java.util.*;

public final class UnicodeOrthographyBuilder extends AbstractUnicodeOrthographyParserListener {

    private OrthographyBuilder builder = new OrthographyBuilder();

    private final Stack<OrthographyBuilder> builderStack = new Stack<>();

    private List<WordElement> wordElements = new ArrayList<>();

    private Langs langs = new Langs();

    private List<Language> langList = new ArrayList<>();

    private Collection<CaDelimiterType> insideCaDelim = new ArrayList<>();

    @Override
    public void exitLinker(UnicodeOrthographyParser.LinkerContext ctx) {
        final LinkerType lt = LinkerType.fromString(ctx.getText());
        builder.append(new Linker(lt));
    }

    @Override
    public void enterComplete_word(UnicodeOrthographyParser.Complete_wordContext ctx) {
        langList = new ArrayList<>();
        langs = new Langs();
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
                boolean separatedPrefix = false;
                String userSpecialForm = "";
                WordType wordType = null;
                WordFormType formType = null;
                String formSuffix = null;
                List<WordPos> pos = new ArrayList<>();
                UntranscribedType untranscribedType = null;
                if (ctx.wordprefix() != null) {
                    wordType = WordType.fromString(ctx.wordprefix().getText());
                    if (wordType == null)
                        throw new OrthoParserException("Invalid word prefix '" + ctx.wordprefix().getText() + "'",
                                ctx.wordprefix().getStart().getCharPositionInLine());
                } else if(isOmission) {
                    isOmission = false;
                    wordType = WordType.OMISSION;
                }
                if (ctx.wordsuffix() != null) {
                    if(ctx.wordsuffix().HASH() != null) {
                        separatedPrefix = true;
                    }
                    if (ctx.wordsuffix().formtype() != null) {
                        formType = WordFormType.fromCode(ctx.wordsuffix().formtype().getText());
                        if(ctx.wordsuffix().formsuffix() != null) {
                            formSuffix = ctx.wordsuffix().formsuffix().getText().substring(1);
                        }
                    }
                    if(ctx.wordsuffix().user_special_form() != null) {
                        userSpecialForm = ctx.wordsuffix().user_special_form().getText().substring(3);
                    }
                    if (ctx.wordsuffix().wordpos() != null) {
                        for(var wordposctx:ctx.wordsuffix().wordpos()) {
                            final String[] categories = wordposctx.getText().substring(1).split(":");
                            if(categories.length > 0) {
                                final String category = categories[0];
                                final List<String> subcategories = new ArrayList<>();
                                for(int i = 1; i < categories.length; i++) subcategories.add(categories[i]);
                                pos.add(new WordPos(category, subcategories));
                            } else {
                                throw new OrthoParserException("Invalid pos", wordposctx.getStart().getCharPositionInLine());
                            }
                        }
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
                WordSuffix suffix = (formType != null || pos != null ? new WordSuffix(separatedPrefix, formType, formSuffix, userSpecialForm, pos) : null);

                builder.annnotateWord(langs, prefix, suffix, untranscribedType);
            }
        }
    }

    private boolean isOmission = false;
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
            } else if(wordElements.get(0) instanceof WordText && "0".equals(wordElements.get(0).text())) {
                builder.append(new Action());
                wordElements.clear();
                return;
            }
        }
        if(wordElements.get(0) instanceof WordText && wordElements.get(0).text().startsWith("0")) {
            isOmission = true;
            WordText fixedWordText = new WordText(wordElements.get(0).text().substring(1));
            wordElements.remove(0);
            wordElements.add(0, fixedWordText);
        }
        builder.appendWord(wordElements);
        wordElements.clear();
    }

    @Override
    public void exitLanguage(UnicodeOrthographyParser.LanguageContext ctx) {
        try {
            final Language lang = Language.parseLanguage(ctx.getText());
            langList.add(lang);
        } catch (IllegalArgumentException e) {
            throw new OrthoParserException("invalid language", ctx.getStart().getCharPositionInLine());
        }
    }

    @Override
    public void exitUttlang(UnicodeOrthographyParser.UttlangContext ctx) {
        if(langList.size() == 0)
            throw new OrthoParserException("no language specified",
                    ctx.LANGUAGE_START().getSymbol().getCharPositionInLine());
        if(langList.size() > 1)
            throw new OrthoParserException("too many languages",
                    ctx.language().getStart().getCharPositionInLine());
        final Language lang = langList.get(0);
        builder.append(new UtteranceLanguage(lang));
        langList = new ArrayList<>();
    }

    @Override
    public void exitSecondaryLanguage(UnicodeOrthographyParser.SecondaryLanguageContext ctx) {
        langs = new Langs(Langs.LangsType.SECONDARY);
    }

    @Override
    public void exitSingleLanguage(UnicodeOrthographyParser.SingleLanguageContext ctx) {
        langs = new Langs(Langs.LangsType.SINGLE, langList);
    }

    @Override
    public void exitAmbiguousLanguages(UnicodeOrthographyParser.AmbiguousLanguagesContext ctx) {
        langs = new Langs(Langs.LangsType.AMBIGUOUS, langList);
    }

    @Override
    public void exitMultipleLanguages(UnicodeOrthographyParser.MultipleLanguagesContext ctx) {
        langs = new Langs(Langs.LangsType.MULTIPLE, langList);
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
            final BeginEnd beginEnd = !insideCaDelim.contains(eleType) ? BeginEnd.BEGIN : BeginEnd.END;
            if(beginEnd == BeginEnd.BEGIN)
                insideCaDelim.add(eleType);
            wordElements.add(new CaDelimiter(beginEnd, eleType));
        } else {
            throw new OrthoParserException(ctx.getText(), ctx.getStart().getCharPositionInLine());
        }
    }

    @Override
    public void exitWord_text(UnicodeOrthographyParser.Word_textContext ctx) {
        wordElements.add(new WordText(ctx.getText()));
    }

    @Override
    public void exitShortening(UnicodeOrthographyParser.ShorteningContext ctx) {
        wordElements.add(new Shortening(ctx.text().getText()));
    }

    @Override
    public void exitTagMarker(UnicodeOrthographyParser.TagMarkerContext ctx) {
        final TagMarkerType tmType = TagMarkerType.fromString(ctx.getText().charAt(0) + "");
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
        final int index = (ctx.number() != null ? Integer.parseInt(ctx.number().getText()) : -1);
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
    public void exitHappening(UnicodeOrthographyParser.HappeningContext ctx) {
        final String text = ctx.text().getText();
        builder.append(new Happening(text));
    }

    @Override
    public void exitOtherSpokenEvent(UnicodeOrthographyParser.OtherSpokenEventContext ctx) {
        final String who = ctx.who().getText();
        final String text = ctx.text().getText();
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
        final int index = (ctx.number() != null ? Integer.parseInt(ctx.number().getText()) : -1);
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
        final String label = ctx.text().getText();
        builder.append(new LongFeature(beginEnd, label));
    }

    @Override
    public void exitNonvocal(UnicodeOrthographyParser.NonvocalContext ctx) {
        BeginEndSimple beginEndSimple =
                ctx.getText().startsWith(Nonvocal.NONVOCAL_START) ? BeginEndSimple.BEGIN : BeginEndSimple.END;
        if(ctx.CLOSE_BRACE() != null) {
            beginEndSimple = BeginEndSimple.SIMPLE;
        }
        final String label = ctx.text().getText();
        builder.append(new Nonvocal(beginEndSimple, label));
    }

    @Override
    public void exitPostcode(UnicodeOrthographyParser.PostcodeContext ctx) {
        final String code = ctx.getText().substring(Postcode.POSTCODE_PREFIX.length(), ctx.getText().length()-1).trim();
        builder.append(new Postcode(code));
    }

    @Override
    public void exitQuotation(UnicodeOrthographyParser.QuotationContext ctx) {
        final BeginEnd beginEnd = ctx.getText().equals(Quotation.QUOTATION_BEGIN) ? BeginEnd.BEGIN : BeginEnd.END;
        builder.append(new Quotation(beginEnd));
    }

    @Override
    public void enterReplacement(UnicodeOrthographyParser.ReplacementContext ctx) {
        builderStack.push(builder);
        builder = new OrthographyBuilder();
    }

    @Override
    public void exitReplacement(UnicodeOrthographyParser.ReplacementContext ctx) {
        final boolean real = ctx.getText().startsWith(Replacement.PREFIX_REAL);
        final Replacement replacement = new Replacement(real, builder.toOrthography().toList().stream().map(e -> (Word)e).toList());
        builder = builderStack.pop();

        final OrthographyElement lastEle = builder.lastElement();
        if (lastEle != null && lastEle instanceof Word) {
            final Word w = (Word) lastEle;
            final List<Replacement> replacements = new ArrayList<>(w.getReplacements());
            replacements.add(replacement);
                builder.replaceLastElement(new Word(w.getLangs(), replacements,
                        w.getPrefix(), w.getSuffix(), w.getUntranscribedType(),
                        w.getWordElements().toArray(new WordElement[0])));
        } else {
            throw new OrthoParserException("replacement without word", ctx.getStart().getCharPositionInLine());
        }
    }

}
