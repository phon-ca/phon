package ca.phon.orthography.mor.parser;

import ca.phon.mor.MorBaseListener;
import ca.phon.mor.MorParser;
import ca.phon.orthography.Terminator;
import ca.phon.orthography.TerminatorType;
import ca.phon.orthography.mor.*;

import java.util.ArrayList;
import java.util.List;

public class MorBuilder extends MorBaseListener {

    final List<MorElement> elements = new ArrayList<>();

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
        elements.add(mw);
    }

    @Override
    public void exitMt(MorParser.MtContext ctx) {
        final TerminatorType tt = TerminatorType.fromString(ctx.getText());
        elements.add(new MorTerminator(tt));
    }

    public List<MorElement> getElements() {
        return this.elements;
    }

}
