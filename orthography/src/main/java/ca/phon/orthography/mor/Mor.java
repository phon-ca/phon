package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.List;

/**
 * A group of words in %mor or %trn.
 */
@Documentation({"https://talkbank.org/manuals/CHAT.html#Morphological_Tier","https://talkbank.org/manuals/CHAT.html#Training_Tier"})
public final class Mor extends MorphemicBaseType {

    private final boolean omitted;

    private final List<MorPre> morPres;

    private final List<MorPost> morPosts;

    public Mor(MorElement element, List<String> translations, List<MorPre> morPres, List<MorPost> morPosts, boolean omitted) {
        super(element, translations);
        this.morPres = morPres;
        this.morPosts = morPosts;
        this.omitted = omitted;
    }

    @Override
    public String text() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getElement().text());
        for(int i = 0; i < getTranslations().size(); i++) {
            builder.append(i == 0 ? "=" : "/");
            builder.append(getTranslations().get(i));
        }
        for(MorPost mp:morPosts)
            builder.append(mp.text());
        return builder.toString();
    }

}
