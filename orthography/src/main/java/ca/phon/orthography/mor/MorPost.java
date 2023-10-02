package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.List;

/**
 * mor postclitic
 */
@Documentation("https://talkbank.org/manuals/MOR.html#MorphologicalPostclitic")
public final class MorPost extends MorphemicBaseType {

    public final static String PREFIX = "~";

    public MorPost(MorElement element, List<MorTranslation> translations) {
        super(element, translations);
    }

    @Override
    public String text() {
        return String.format("%s%s", PREFIX, getElement().text());
    }

}
