package ca.phon.orthography;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;

import java.util.Optional;
import java.util.Set;

public final class TagMarker extends AbstractOrthoElement {

    private final TagMarkerType type;

    public TagMarker(TagMarkerType type) {
        super();
        this.type = type;
    }

    public TagMarkerType getType() {
        return type;
    }

    @Override
    public String text() {
        return type.getChar() + "";
    }

}
