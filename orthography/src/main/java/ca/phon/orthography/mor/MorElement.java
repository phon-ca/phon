package ca.phon.orthography.mor;

import ca.phon.extensions.ExtendableObject;

/**
 * An element on the mor tier
 */
public abstract class MorElement extends ExtendableObject {

    public MorElement() {
        super();
    }

    public abstract String text();

    @Override
    public String toString() {
        return text();
    }

}
