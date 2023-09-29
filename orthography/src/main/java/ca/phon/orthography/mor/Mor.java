package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.List;

/**
 * A group of words in %mor or %trn.
 */
@Documentation({"https://talkbank.org/manuals/CHAT.html#Morphological_Tier","https://talkbank.org/manuals/CHAT.html#Training_Tier"})
public class Mor extends MorBase {

    private final boolean omitted;

    private final List<MorPre> morPres;

    private final List<MorPost> morPosts;

    public Mor(MorElement element, List<MorTranslation> translations, List<MorPre> morPres, List<MorPost> morPosts, boolean omitted) {
        super(element, translations);
        this.morPres = morPres;
        this.morPosts = morPosts;
        this.omitted = omitted;
    }

}
