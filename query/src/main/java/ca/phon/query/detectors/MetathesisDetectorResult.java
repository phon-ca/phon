package ca.phon.query.detectors;

import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.features.FeatureSet;

public class MetathesisDetectorResult extends DetectorResult {
	
	private FeatureSet features1;
	
	private FeatureSet features2;

	public MetathesisDetectorResult(PhoneMap phoneMap) {
		super(phoneMap);
	}

	public FeatureSet getFeatures1() {
		return features1;
	}

	public void setFeatures1(FeatureSet features1) {
		this.features1 = features1;
	}

	public FeatureSet getFeatures2() {
		return features2;
	}

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
