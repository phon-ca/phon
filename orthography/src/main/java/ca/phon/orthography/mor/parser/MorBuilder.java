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

public class MorBuilder extends MorBaseListener {

    private record MorData(AtomicReference<MorElement> elementRef, List<MorTranslation> translations,
                           List<MorPre> morPres, List<MorPost> morPosts, AtomicReference<Boolean> omittedRef) {
    }
    final Stack<MorData> morDataStack = new Stack<>();

    private MorData createEmptyData() {
        return new MorData(new AtomicReference<>(), new ArrayList<>(),
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
        final Mor mor = new Mor(morData.elementRef().get(), morData.translations(),
                morData.morPres(), morData.morPosts(), morData.omittedRef().get());
        mors.add(mor);
    }

    @Override
    public void exitMw(MorParser.MwContext ctx) {
        final List<String> prefixList = new ArrayList<>();
        for(MorParser.PrefixContext pctx:ctx.prefix()) {
            prefixList.add(pctx.string().getText());
        }
        final MorParser.PosContext posCtx = ctx.pos();
        final String category = posCtx.category().getText();
        final List<String> subcategories = new ArrayList<>();
        for(MorParser.SubcategoryContext subCtx:posCtx.subcategory()) {
            subcategories.add(subCtx.string().getText());
        }
        final Pos pos = new Pos(category, subcategories);
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
        morDataStack.peek().elementRef().set(mw);
    }

    @Override
    public void exitMt(MorParser.MtContext ctx) {
        final TerminatorType tt = TerminatorType.fromString(ctx.getText());
        morDataStack.peek().elementRef().set(new MorTerminator(tt));
    }

    @Override
    public void enterMorpost(MorParser.MorpostContext ctx) {
        morDataStack.push(createEmptyData());
    }

    @Override
    public void exitMorpost(MorParser.MorpostContext ctx) {
        final MorData morData = morDataStack.pop();
        final MorPost morPost = new MorPost(morData.elementRef().get(), morData.translations());
        morDataStack.peek().morPosts().add(morPost);
    }

    public List<Mor> getMors() {
        return this.mors;
    }

}
