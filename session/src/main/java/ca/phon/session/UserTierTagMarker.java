package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.orthography.TagMarkerType;

public final class UserTierTagMarker extends ExtendableObject implements UserTierElement {

    private final TagMarkerType tagMarkerType;

    public UserTierTagMarker(TagMarkerType tagMarkerType) {
        super();
        this.tagMarkerType = tagMarkerType;
    }

    public TagMarkerType getType() {
        return this.tagMarkerType;
    }

    @Override
    public String text() {
        return this.tagMarkerType.toString();
    }

    @Override
    public String toString() {
        return text();
    }

}
