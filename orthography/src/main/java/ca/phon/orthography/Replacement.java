package ca.phon.orthography;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Replacement extends AbstractOrthographyElement {

    public static final String PREFIX_REAL = "[::";

    public static final String PREFIX = "[:";

    private final boolean real;

    private final List<Word> words;

    public Replacement(Word ... words) {
        this(false, Arrays.asList(words));
    }

    public Replacement(boolean real, Word ... words) {
        this(real, Arrays.asList(words));
    }

    public Replacement(boolean real, List<Word> words) {
        super();
        this.real = real;
        this.words = Collections.unmodifiableList(words);
    }

    public boolean isReal() {
        return real;
    }

    public List<Word> getWords() {
        return words;
    }

    public String getWordText() {
        return getWords().stream().map(w -> w.text()).collect(Collectors.joining(" "));
    }

    @Override
    public String text() {
        final String prefix = isReal() ? PREFIX_REAL : PREFIX;
        return String.format("%s %s]", prefix, getWordText());
    }

}
