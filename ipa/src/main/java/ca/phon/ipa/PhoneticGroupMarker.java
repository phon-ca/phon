package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;

/**
 * Small pinchers on the main line to denote one to two alignment
 */
public final class PhoneticGroupMarker extends IPAElement {

    private final PhoneticGroupMarkerType type;

    public PhoneticGroupMarker(PhoneticGroupMarkerType type) {
        super();
        this.type = type;
    }

    public PhoneticGroupMarkerType getType() {
        return type;
    }

    @Override
    protected FeatureSet _getFeatureSet() {
        return new FeatureSet();
    }

    @Override
    public String getText() {
        return type.toString();
    }

}
