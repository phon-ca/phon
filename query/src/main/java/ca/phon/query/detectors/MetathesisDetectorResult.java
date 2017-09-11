package ca.phon.query.detectors;

import java.util.List;

import ca.phon.ipa.*;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.features.FeatureSet;

public class MetathesisDetectorResult extends DetectorResult {
	
	private PhoneticProfile profile1 = new PhoneticProfile();
	
	private PhoneticProfile profile2 = new PhoneticProfile();
	
	private FeatureSet features1;
	
	private FeatureSet features2;

	public MetathesisDetectorResult(PhoneMap phoneMap) {
		super(phoneMap);
	}
	
	public PhoneticProfile getProfile1() {
		return profile1;
	}

	public void setProfile1(PhoneticProfile profile1) {
		this.profile1 = profile1;
	}

	public PhoneticProfile getProfile2() {
		return profile2;
	}

	public void setProfile2(PhoneticProfile profile2) {
		this.profile2 = profile2;
	}

	@Deprecated
	public FeatureSet getFeatures1() {
		return features1;
	}

	@Deprecated
	public void setFeatures1(FeatureSet features1) {
		this.features1 = features1;
	}

	@Deprecated
	public FeatureSet getFeatures2() {
		return features2;
	}

	@Deprecated
	public void setFeatures2(FeatureSet features2) {
		this.features2 = features2;
	}
	
	@Override
	public String toString() {
		final PhoneMap map = getPhoneMap();
		if(map == null) return "";

		final String ELLIPSIS = "\u2026";
		List<IPAElement> elems1 = map.getAlignedElements(this.pos1);
		List<IPAElement> elems2 = map.getAlignedElements(this.pos2);

		// Set up target string
		String sTarget = (elems1.get(0) != null
				? elems1.get(0).toString()
						: " ");
		if(pos1 != pos2 - 1) sTarget += ELLIPSIS;
		sTarget += (elems2.get(0) != null
				? elems2.get(0).toString()
						: " ");
		if(pos1 > 0) sTarget = ELLIPSIS + sTarget;
		if(pos2 < map.getAlignmentLength() - 1) sTarget = sTarget + ELLIPSIS;

		// Set up actual string
		String sActual = (elems1.get(1) != null
				? elems1.get(1).toString()
						: " ");
		if(pos1 != pos2 - 1) sActual += ELLIPSIS;
		sActual += (elems2.get(1) != null
				? elems2.get(1).toString()
						: " ");
		if(pos1 > 0) sActual = ELLIPSIS + sActual;
		if(pos2 < map.getAlignmentLength() - 1) sActual = sActual + ELLIPSIS;

		return String.format(
				"%s \u2192 %s",
				sTarget, sActual);
	}
	
}
