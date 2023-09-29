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

    public float getStartTime() {
        return internalMedia.getStartTime();
    }

    public float getEndTime() {
        return internalMedia.getEndTime();
    }

    public boolean isUnset() {
        return internalMedia.isUnset();
    }

    public boolean isPoint() {
        return internalMedia.isPoint();
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
