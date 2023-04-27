package ca.phon.orthography.parser;

import ca.phon.orthography.*;
import ca.phon.orthography.parser.exceptions.OrthoParserException;

import java.util.ArrayList;
import java.util.List;

public final class UnicodeOrthographyBuilder extends AbstractUnicodeOrthographyParserListener {

    private final OrthographyBuilder builder = new OrthographyBuilder();

    private List<OrthoWordElement> wordElements = new ArrayList<>();

    @Override
    public void exitLinker(UnicodeOrthographyParser.LinkerContext ctx) {
        final LinkerType lt = LinkerType.fromString(ctx.getText());
        builder.append(new Linker(lt));
    }

    @Override
    public void exitComplete_word(UnicodeOrthographyParser.Complete_wordContext ctx) {
        WordType wordType = null;
        WordFormType formType = null;
        String pos = null;
        UntranscribedType untranscribedType = null;
        if(ctx.wordprefix() != null) {
            wordType = WordType.fromCode(ctx.wordprefix().getText());
            // TODO issue warning if wordType is null
        }
        if(ctx.wordsuffix() != null) {
            if(ctx.wordsuffix().formtype() != null) {
                formType = WordFormType.fromCode(ctx.wordsuffix().formtype().getText());
            }
            if(ctx.wordsuffix().wordpos() != null) {
                pos = ctx.wordsuffix().wordpos().getText();
            }
        }
        if(wordElements.size() == 1 && wordElements.get(0).getText().equals("xxx")) {
            untranscribedType = UntranscribedType.UNINTELLIGIBLE;
        } else if(wordElements.size() == 1 && wordElements.get(0).getText().equals("yyy")) {
            untranscribedType = UntranscribedType.UNINTELLIGIBLE_WORD_WITH_PHO;
        } else if(wordElements.size() == 1 && wordElements.get(0).getText().equals("www")) {
            untranscribedType = UntranscribedType.UNTRANSCRIBED;
        }
        WordPrefix prefix = (wordType == null ? null : new WordPrefix(wordType));
        WordSuffix suffix = (formType != null || pos != null ? new WordSuffix(formType, null, null, pos) : null);

        builder.annnotateWord(prefix, suffix, untranscribedType);
    }

    @Override
    public void exitSingleWord(UnicodeOrthographyParser.SingleWordContext ctx) {
        builder.appendWord(wordElements);
        wordElements.clear();
    }

    @Override
    public void exitCompoundWord(UnicodeOrthographyParser.CompoundWordContext ctx) {
        OrthoCompoundWordMarkerType type = OrthoCompoundWordMarkerType.fromMarker(ctx.wk().getText().charAt(0));
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
            throw new IllegalArgumentException(ctx.getText());
        }
    }

    @Override
    public void exitText(UnicodeOrthographyParser.TextContext ctx) {
        wordElements.add(new OrthoWordText(ctx.getText()));
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
    public void exitTerminator(UnicodeOrthographyParser.TerminatorContext ctx) {
        final TerminatorType tt = TerminatorType.fromString(ctx.getText());
        builder.append(new Terminator(tt));
    }

    public Orthography getOrthography() {
        return builder.toOrthography();
    }

}
