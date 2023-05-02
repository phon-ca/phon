package ca.phon.orthography;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Multiple words with a single phonetic transcription.
 */
public final class PhoneticGroup extends AbstractOrthographyElement {

    public final static String PHONETIC_GROUP_START = "\u2039";
    public final static String PHONETIC_GROUP_END = "\u203a";

    private List<OrthographyElement> elements;

    public PhoneticGroup(List<OrthographyElement> elements) {
        super();
        this.elements = Collections.unmodifiableList(elements);
    }

    public List<OrthographyElement> getElements() {
        return this.elements;
    }

    @Override
    public String text() {
        final String eleText = getElements().stream().map(ele -> ele.text()).collect(Collectors.joining(" "));
        final String start = getElements().size() > 1 ? PHONETIC_GROUP_START : "";
        final String end = getElements().size() > 1 ? PHONETIC_GROUP_END : "";
        return String.format("%s%s%s", start, eleText, end);
    }

}
