package ca.phon.orthography.mor.parser;

import ca.phon.mor.MorBaseListener;
import ca.phon.mor.MorParser;
import ca.phon.orthography.Terminator;
import ca.phon.orthography.TerminatorType;
import ca.phon.orthography.mor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MorBuilder extends MorBaseListener {

    private record MorData(List<MorElement> elements, List<MorTranslation> translations,
                           List<MorPre> morPres, List<MorPost> morPosts, AtomicReference<Boolean> omittedRef) {
    }
    private final Stack<MorData> morDataStack = new Stack<>();

    private MorData createEmptyData() {
        return new MorData(new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new AtomicReference<>(false));
    }

    private final List<Mor> mors = new ArrayList<>();

    @Override
    public void enterMor(MorParser.MorContext ctx) {
        morDataStack.push(createEmptyData());
    }

    @Override
    public void exitMor(MorParser.MorContext ctx) {
        final MorData morData = morDataStack.pop();
        if(morData.elements().size() != 1)
            throw new IllegalStateException("Invalid number of elements for mor " + morData.elements().size());
        final Mor mor = new Mor(morData.elements().get(0), morData.translations(),
                morData.morPres(), morData.morPosts(), morData.omittedRef().get());
        mors.add(mor);
    }

    private Pos readPos(MorParser.PosContext ctx) {
        final String category = ctx.category().getText();
        final List<String> subcategories = new ArrayList<>();
        for(MorParser.SubcategoryContext subCtx:ctx.subcategory()) {
            subcategories.add(subCtx.string().getText());
        }
        final Pos pos = new Pos(category, subcategories);
        return pos;
    }

    @Override
    public void exitMw(MorParser.MwContext ctx) {
        final List<String> prefixList = new ArrayList<>();
        for(MorParser.PrefixContext pctx:ctx.prefix()) {
            prefixList.add(pctx.string().getText());
        }
        final MorParser.PosContext posCtx = ctx.pos();
        final Pos pos = readPos(posCtx);
        final String stem = ctx.stem().string().getText();
        final List<MorMarker> markers = new ArrayList<>();
        for(MorParser.MarkerContext mctx:ctx.marker()) {
            final char typeCh = mctx.getText().charAt(0);
            final MorMarkerType morMarkerType = switch (typeCh) {
                case '-' -> MorMarkerType.Suffix;
                case '&' -> MorMarkerType.SuffixFusion;
                case ':' -> MorMarkerType.MorCategory;
                default -> throw new IllegalArgumentException("Invalid marker prefix " + typeCh);
            };
            final MorMarker morMarker = new MorMarker(morMarkerType, mctx.getText().substring(1));
            markers.add(morMarker);
        }
        final MorWord mw = new MorWord(prefixList, pos, stem, markers);
        morDataStack.peek().elements().add(mw);
    }

    @Override
    public void enterMwc(MorParser.MwcContext ctx) {
        morDataStack.push(createEmptyData());
    }

    @Override
    public void exitMwc(MorParser.MwcContext ctx) {
        final MorData morData = morDataStack.pop();
        if(morData.elements().isEmpty()) {
            throw new IllegalStateException("Must have words in a compound");
        }
        for(MorElement morElement:morData.elements()) {
            if(!(morElement instanceof MorWord)) {
                throw new IllegalStateException("Mor content in compound must be a word");
            }
        }
        final List<MorWord> words = morData.elements().stream().map(ele -> (MorWord)ele).toList();
        final List<String> prefixList = new ArrayList<>();
        for(MorParser.PrefixContext pctx:ctx.prefix()) {
            prefixList.add(pctx.string().getText());
        }
        final MorParser.PosContext posCtx = ctx.pos();
        final Pos pos = readPos(posCtx);
        final MorWordCompound mwc = new MorWordCompound(prefixList, pos, words);
        morDataStack.peek().elements.add(mwc);
    }

    @Override
    public void exitMt(MorParser.MtContext ctx) {
        final TerminatorType tt = TerminatorType.fromString(ctx.getText());
        morDataStack.peek().elements.add(new MorTerminator(tt));
    }

    @Override
    public void exitTranslation(MorParser.TranslationContext ctx) {
        final String translation = ctx.getText().substring(1);
        morDataStack.peek().translations().add(new MorTranslation(translation));
    }

    @Override
    public void enterMorpost(MorParser.MorpostContext ctx) {
        morDataStack.push(createEmptyData());
    }

    @Override
    public void exitMorpost(MorParser.MorpostContext ctx) {
        final MorData morData = morDataStack.pop();
        final MorPost morPost = new MorPost(morData.elements().get(0), morData.translations());
        morDataStack.peek().morPosts().add(morPost);
    }

    public List<Mor> getMors() {
        return this.mors;
    }

}
