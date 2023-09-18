package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * CA delimited material with begin/end.
 *
 * <p>
 *     <a href="https://talkbank.org/manuals/CHAT.html#CA_Delimiters">CHAT manual section on
 *                     this topic...</a>
 * </p>
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#CA_Delimiters")
public final class CaDelimiter extends AbstractWordElement {

    private final BeginEnd beginEnd;

    private final CaDelimiterType type;

    public CaDelimiter(BeginEnd beginEnd, CaDelimiterType type) {
        super();
        this.beginEnd = beginEnd;
        this.type = type;
    }

    public BeginEnd getBeginEnd() { return this.beginEnd; }

    public CaDelimiterType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.toString();
    }

}
