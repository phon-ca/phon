package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.Collections;
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

    public boolean isOmitted() {
        return omitted;
    }

    public List<MorPre> getMorPres() {
        return Collections.unmodifiableList(morPres);
    }

    public List<MorPost> getMorPosts() {
        return Collections.unmodifiableList(morPosts);
    }

    @Override
    public String text() {
        final StringBuilder builder = new StringBuilder();
        if(isOmitted()) {
            builder.append("0");
        }
        for(MorPre morPre:morPres)
            builder.append(morPre);
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
