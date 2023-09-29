package ca.phon.orthography.mor;

import ca.phon.extensions.ExtendableObject;
import ca.phon.util.Documentation;

/**
 * Morphemic translation: =word
 */
@Documentation("https://talkbank.org/manuals/MOR.html#Mor_Translation")
public final class MorTranslation extends ExtendableObject {

    private final static String PREFIX = "=";

    private final String translation;

    public MorTranslation(String translation) {
        super();
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }

    @Override
    public String toString() {
        return String.format("%s%s", PREFIX, getTranslation());
    }

}
