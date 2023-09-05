package ca.phon.session.tierdata;

import ca.phon.extensions.ExtendableObject;
import ca.phon.orthography.InternalMedia;

public class TierInternalMedia extends ExtendableObject implements TierElement {

    private final InternalMedia internalMedia;

    public TierInternalMedia(InternalMedia internalMedia) {
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
