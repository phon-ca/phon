package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.List;

/**
 * morphemic "word": the unit of a %mor line corresponding to a single non-compound word on the
 * main line.
 */
@Documentation("https://talkbank.org/manuals/MOR.html#Mor_Simple_Word")
public final class MorWord extends MorElement {

    /**
     * Morphemic prefix: word#
     */
    private final List<String> prefixList;

    /**
     * Part of speech
     */
    private final Pos pos;

    /**
     * Stem
     */
    private final String stem;

    /**
     * Markers (suffix)
     */
    private final List<MorMarker> markers;

    public MorWord(List<String> prefixList, Pos pos, String stem, List<MorMarker> markers) {
        super();
        this.prefixList = prefixList;
        this.pos = pos;
        this.stem = stem;
        this.markers = markers;
    }

    @Override
    public String text() {
        final StringBuilder builder = new StringBuilder();
        for(String prefix:prefixList) {
            builder.append(prefix).append("#");
        }
        builder.append(pos);
        builder.append("|");
        builder.append(stem);
        for(MorMarker marker:markers) {
            builder.append(marker);
        }
        return builder.toString();
    }

}
