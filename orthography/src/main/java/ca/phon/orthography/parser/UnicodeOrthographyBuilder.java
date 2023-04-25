package ca.phon.orthography.parser;

import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyBuilder;
import ca.phon.orthography.TagMarker;
import ca.phon.orthography.TagMarkerType;

public final class UnicodeOrthographyBuilder extends AbstractUnicodeOrthographyParserListener {

    final OrthographyBuilder builder = new OrthographyBuilder();

    @Override
    public void exitWord(UnicodeOrthographyParser.WordContext ctx) {
        builder.append(ctx.getText());
    }

    @Override
    public void exitTagMarker(UnicodeOrthographyParser.TagMarkerContext ctx) {
        final TagMarkerType tmType = TagMarkerType.fromChar(ctx.getText().charAt(0));
        builder.append(new TagMarker(tmType));
    }

    public Orthography getOrthography() {
        return builder.toOrthography();
    }

}
