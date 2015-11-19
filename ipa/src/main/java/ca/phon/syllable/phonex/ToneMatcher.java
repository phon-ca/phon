package ca.phon.syllable.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.syllable.SyllabificationInfo;

/**
 * Matcher for tone information in {@link SyllabificationInfo}
 *
 */
public class ToneMatcher implements PhoneMatcher {
	
	private FeatureSet toneFeatures;
	
	public ToneMatcher(FeatureSet features) {
		this.toneFeatures = features;
	}

	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = false;
		
		final SyllabificationInfo info = p.getExtension(SyllabificationInfo.class);
		if(info != null) {
			FeatureSet intersection = FeatureSet.intersect(toneFeatures, info.getToneFeatures());
			retVal = (intersection.size() > 0);
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		final FeatureSet allToneFeatures = FeatureSet.fromArray(new String[] { "tone1", "tone2",
				"tone3", "tone4", "tone5", "tone6", "tone7", "tone8", "tone9" });
		return toneFeatures.equals(allToneFeatures);
	}

}
