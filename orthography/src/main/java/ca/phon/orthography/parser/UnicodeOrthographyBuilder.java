package ca.phon.orthography.parser;

import ca.phon.orthography.*;

public final class UnicodeOrthographyBuilder extends AbstractUnicodeOrthographyParserListener {

    final OrthographyBuilder builder = new OrthographyBuilder();

    @Override
    public void exitLinker(UnicodeOrthographyParser.LinkerContext ctx) {
        final LinkerType lt = LinkerType.fromString(ctx.getText());
        builder.append(new Linker(lt));
    }

    @Override
    public void exitWord(UnicodeOrthographyParser.WordContext ctx) {
        builder.append(ctx.getText());
    }

    @Override
    public void exitTagMarker(UnicodeOrthographyParser.TagMarkerContext ctx) {
        final TagMarkerType tmType = TagMarkerType.fromChar(ctx.getText().charAt(0));
        builder.append(new TagMarker(tmType));
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
