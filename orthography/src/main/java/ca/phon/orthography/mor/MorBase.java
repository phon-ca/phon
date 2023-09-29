package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

import java.util.List;

/**
 * %mor unit of one-to-one correspondence with main line. A single word or a compound word or a terminator.
 */
@Documentation("https://talkbank.org/manuals/MOR.html#Morphological_Word")
public abstract class MorBase {

    private final MorElement element;

    private final List<MorTranslation> translations;

    public MorBase(MorElement element, List<MorTranslation> translations) {
        super();
        this.element = element;
        this.translations = translations;
    }

}
