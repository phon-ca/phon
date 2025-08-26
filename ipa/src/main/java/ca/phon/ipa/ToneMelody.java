package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;

/**
 * Represents a tone melody in an IPA transcription.
 * A tone melody is a sequence of tones that can be
 * associated with a syllable or word.
 */
public class ToneMelody extends IPAElement {

    private final ToneNumber[] melody;

    public ToneMelody(ToneNumber... melody) {
        this(melody, null, null);
    }

    public ToneMelody(ToneNumber[] melody, FeatureSet overrideFeatureSet, SyllableInfo syllableInfo) {
        super(overrideFeatureSet, syllableInfo != null ? syllableInfo : new SyllableInfo(SyllableConstituentType.TONENUMBER));
        this.melody = melody;
    }

    @Override
    protected FeatureSet _getFeatureSet() {
        FeatureSet retVal = new FeatureSet();
        for(ToneNumber tn: melody) {
            retVal = FeatureSet.union(retVal, tn.featureSet());
        }
        return retVal;
    }

    @Override
    public String getText() {
        final StringBuilder sb = new StringBuilder();
        for(ToneNumber tn: melody) {
            sb.append(tn.getText());
        }
        return sb.toString();
    }
}
