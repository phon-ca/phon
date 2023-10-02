package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.Collections;
import java.util.List;

/**
 * %mor unit of one-to-one correspondence with main line. A single word or a compound word or a terminator.
 */
@Documentation("https://talkbank.org/manuals/MOR.html#Morphological_Word")
public abstract class MorphemicBaseType {

    private final MorElement element;

    private final List<MorTranslation> translations;

    public MorphemicBaseType(MorElement element, List<MorTranslation> translations) {
        super();
        this.element = element;
        this.translations = translations;
    }

    public MorElement getElement() {
        return this.element;
    }

    public List<MorTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    public abstract String text();

    @Override
    public String toString() {
        return text();
    }

}
