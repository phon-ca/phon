package ca.phon.session;

import ca.phon.extensions.ExtendableObject;

/**
 * Tier data enclosed in parentheses. Comments are not included
 * in tier alignment.
 */
public final class UserTierComment extends ExtendableObject implements UserTierElement {

    public final static String PREFIX = "(";

    public final static String SUFFIX = ")";

    private final String text;

    public UserTierComment(String text) {
        super();
        this.text = text;
    }

    @Override
    public String text() {
        return this.text;
    }

    @Override
    public String toString() {
        return String.format("%s%s%s", PREFIX, text(), SUFFIX);
    }

}
