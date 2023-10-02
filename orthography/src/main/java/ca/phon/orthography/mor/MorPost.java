package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.List;

/**
 * mor postclitic
 */
@Documentation("https://talkbank.org/manuals/MOR.html#MorphologicalPostclitic")
public final class MorPost extends MorphemicBaseType {

    public final static String PREFIX = "~";

    public MorPost(MorElement element, List<String> translations) {
        super(element, translations);
    }

    @Override
    public String text() {
        final StringBuilder builder = new StringBuilder();
        builder.append(PREFIX);
        builder.append(getElement());
        for(int i = 0; i < getTranslations().size(); i++) {
            builder.append(i == 0 ? "=" : "/");
            builder.append(getTranslations().get(i));
        }
        return builder.toString();
    }

}
