package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.List;

/**
 * mor preclitic
 */
@Documentation("https://talkbank.org/manuals/MOR.html#MorphologicalPreclitic")
public final class MorPre extends MorphemicBaseType {

    public final static String SUFFIX = "$";

    public MorPre(MorElement element, List<String> translations) {
        super(element, translations);
    }

    @Override
    public String text() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getElement());
        for(int i = 0; i < getTranslations().size(); i++) {
            builder.append(i == 0 ? "=" : "/");
            builder.append(getTranslations().get(i));
        }
        builder.append(SUFFIX);
        return builder.toString();
    }

}
