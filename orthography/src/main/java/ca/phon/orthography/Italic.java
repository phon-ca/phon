package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Italic begin/end marker
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#Italic")
public class Italic extends AbstractOrthographyElement implements WordElement {

    private final BeginEnd beginEnd;

    public Italic(BeginEnd beginEnd) {
        super();
        this.beginEnd = beginEnd;
    }

    public BeginEnd getBeginEnd() {
        return beginEnd;
    }

    @Override
    public String text() {
        return "";
    }

}
