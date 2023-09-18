package ca.phon.orthography;

import ca.phon.util.Language;

/**
 * [- lang] - Utterance language, specified at beginning of main line.
 *
 * lang must be a three letter language code, followed by optional tags
 * up to 8 characters long separated by a '-'.
 *
 * e.g., <pre>eng</pre>, <pre>fra-ca</pre>
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#_Toc133061328")
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
