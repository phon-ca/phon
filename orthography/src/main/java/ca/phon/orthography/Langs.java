package ca.phon.orthography;

import ca.phon.util.Language;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Language specified at the word level
 */
public final class Langs {

    public final static String PREFIX = "@s";

    public enum LangsType {
        // default
        UNSPECIFIED,
        // session secondary language
        SECONDARY,
        // single language - specified
        SINGLE,
        // combination of multiple languages
        MULTIPLE,
        // may be one of multiple languages
        AMBIGUOUS;
    };

    private final LangsType type;

    private final List<Language> langs;

    public Langs() {
        this(LangsType.UNSPECIFIED);
    }

    public Langs(LangsType type, String ... langs) {
        this(type, Arrays.stream(langs).map((lang) -> Language.parseLanguage(lang)).toList());
    }

    public Langs(LangsType type, List<Language> langs) {
        super();
        this.type = type;
        this.langs = Collections.unmodifiableList(langs);
    }

    public LangsType getType() {
        return type;
    }

    public List<Language> getLangs() {
        return langs;
    }

    @Override
    public String toString() {
        final String prefix = switch (getType()) {
            case SINGLE, MULTIPLE, AMBIGUOUS -> PREFIX + ":";
            case SECONDARY -> PREFIX;
            case UNSPECIFIED -> "";
        };
        final String delim = switch (getType()) {
            case SINGLE, UNSPECIFIED, SECONDARY -> "";
            case MULTIPLE -> "+";
            case AMBIGUOUS -> "&";
        };
        final String langText = getLangs().stream()
                .map(l -> l.toString())
                .collect(Collectors.joining(delim));
        return String.format("%s%s", prefix, langText);
    }

}
