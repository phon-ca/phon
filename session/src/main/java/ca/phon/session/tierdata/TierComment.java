package ca.phon.session.tierdata;

import ca.phon.extensions.ExtendableObject;

/**
 * Tier data enclosed in parentheses. Comments are not included
 * in tier alignment.
 */
public final class TierComment extends ExtendableObject implements TierElement {

    public final static String PREFIX = "[%";

    public final static String SUFFIX = "]";

    private final String text;

    public TierComment(String text) {
        super();
        this.text = text;
    }

    @Override
    public String text() {
        return this.text;
    }

    @Override
    public String toString() {
        return String.format("%s %s%s", PREFIX, text(), SUFFIX);
    }

}
