package ca.phon.orthography.mor;

import java.util.List;

public final class MorPre extends MorphemicBaseType {

    public MorPre(MorElement element, List<MorTranslation> translations) {
        super(element, translations);
    }

    @Override
    public String text() {
        return "";
    }

}
