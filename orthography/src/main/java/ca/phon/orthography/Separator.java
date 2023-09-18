package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Separator or tone direction marker.
 *
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#Separators")
public final class Separator extends AbstractOrthographyElement {

    private final SeparatorType type;

    public Separator(SeparatorType type) {
        super();
        this.type = type;
    }

    public SeparatorType getType() {
        return type;
    }

    @Override
    public String text() {
        return getType().getText();
    }

}
