package ca.phon.ipa.elements;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Represents a syllable boundary between words.
 * While other types of {@link IPAElement}s can also
 * represent a syllable boundary, this is specifically
 * for the 'forced' syllable boundary marked by a '.'
 * in the transcript.
 * 
 */
public final class SyllableBoundary extends IPAElement {
	
	public SyllableBoundary() {
		super();
		
		setScType(SyllableConstituentType.SYLLABLEBOUNDARYMARKER);
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		// TODO Fix feature set for syllable boundary
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return ".";
	}

}
