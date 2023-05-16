package ca.phon.orthography;

import ca.phon.util.Language;

public class UtteranceLanguage extends AbstractOrthographyElement {

    public final static String PREFIX = "[-";

    private final Language language;

    public UtteranceLanguage(Language language) {
        super();
        this.language = language;
    }

    public Language getLanguage() {
        return language;
    }

    @Override
    public String text() {
        return String.format("%s %s]", PREFIX, getLanguage().toString());
    }

}
