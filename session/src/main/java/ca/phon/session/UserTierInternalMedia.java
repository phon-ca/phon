package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.orthography.InternalMedia;

public class UserTierInternalMedia extends ExtendableObject implements UserTierElement {

    private final InternalMedia internalMedia;

    public UserTierInternalMedia(InternalMedia internalMedia) {
        super();

        this.internalMedia = internalMedia;
    }

    public InternalMedia getInternalMedia() {
        return internalMedia;
    }

    @Override
    public String text() {
        return internalMedia.text();
    }

    @Override
    public String toString() {
        return text();
    }

}
