package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.Collections;
import java.util.List;

/**
 * Morphemic "compound word" using +
 */
@Documentation("https://talkbank.org/manuals/MOR.html#MorphologicalCompound")
public final class MorWordCompound extends MorElement {

    private final List<String> prefixList;

    private final Pos pos;

    private final List<MorWord> words;

    public MorWordCompound(List<String> prefixList, Pos pos, List<MorWord> words) {
        super();
        this.prefixList = prefixList;
        this.pos = pos;
        this.words = words;
    }

    public List<String> getPrefixList() {
        return Collections.unmodifiableList(prefixList);
    }

    public Pos getPos() {
        return pos;
    }

    public List<MorWord> getWords() {
        return Collections.unmodifiableList(words);
    }

    @Override
    public String text() {
        final StringBuilder builder = new StringBuilder();
        for(String prefix:getPrefixList()) {
            builder.append(prefix).append("#");
        }
        builder.append(getPos());
        builder.append("|");
        for(MorWord mw:getWords()) {
            builder.append("+").append(mw);
        }
        return builder.toString();
    }

}
