package ca.phon.ipa.alignment;

import java.util.List;

import ca.phon.alignment.*;
import ca.phon.ipa.*;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.*;

public class PhoneAligner extends IndelAligner<IPAElement> {

	private IPATranscript targetRep;

	private IPATranscript actualRep;

	private List<IPATranscript> targetSylls;

	private List<IPATranscript> actualSylls;

	private boolean hasStressedSyllables = false;

	public PhoneAligner() {
	}

	@Override
	protected int costSubstitute(IPAElement ele1, IPAElement ele2) {
		int tally = 0;

		if( (ele1.getFeatureSet().hasFeature("Consonant")
				&& ele2.getFeatureSet().hasFeature("Consonant")) ) {
			++tally;
		} else if( (ele1.getFeatureSet().hasFeature("Vowel")
				&& ele2.getFeatureSet().hasFeature("Vowel")) ) {
			++tally;
		} else {
			final IPAElement vowel = (ele1.getFeatureSet().hasFeature("Vowel") ? ele1 : ele2);
			final IPAElement notvowel = (vowel == ele1 ? ele2 : ele1);

			if(notvowel.getFeatureSet().hasFeature("syllabic")) {
				return 1;
			} else {
				// align if toString() matches
				if(ele1.toString().equals(ele2.toString())) {
					return 2;
				} else {
					return -1;
				}
			}
		}

		final SyllableConstituentType t1 = ele1.getScType();
		final SyllableConstituentType t2 = ele2.getScType();
		if(t1 == t2) {
			++tally;
		} else {
			--tally;
		}

		if(hasStressedSyllables) {
			final SyllableStress s1 = stressForElement(ele1);
			final SyllableStress s2 = stressForElement(ele2);
			if(s1 == s2) {
				++tally;
			} else {
				--tally;
			}
		}

		final PhoneticProfile p1 = new PhoneticProfile(ele1);
		final PhoneticProfile p2 = new PhoneticProfile(ele2);
		int featureTally = 0;
		if(p1.isConsonant()) {
			featureTally += checkDimension(p1, p2, PhoneDimension.PLACE);
			featureTally += checkDimension(p1, p2, PhoneDimension.MANNER);
//			featureTally += checkDimension(p1, p2, PhoneDimension.VOICING);
		} else {
			featureTally += checkDimension(p1, p2, PhoneDimension.HEIGHT);
			featureTally += checkDimension(p1, p2, PhoneDimension.BACKNESS);
			featureTally += checkDimension(p1, p2, PhoneDimension.TENSENESS);
//			featureTally += checkDimension(p1, p2, PhoneDimension.ROUNDING);
		}
		// if no features match, only subtract 1
		tally += Math.max(-1, featureTally);

		return tally;
	}

	private int checkDimension(PhoneticProfile p1, PhoneticProfile p2, PhoneDimension dimension) {
		int retVal = 0;

		final FeatureSet fs1 = FeatureSet.intersect(p1.get(dimension), dimension.getTerminalFeatures());
		final FeatureSet fs2 = FeatureSet.intersect(p2.get(dimension), dimension.getTerminalFeatures());

		// return if any terminal features intersect
		if(fs1.intersects(fs2))
			retVal = 1;
		else
			retVal = -1;

		return retVal;
	}

	@Override
	protected int costSkip(IPAElement ele) {
		return 0;
	}

	public List<IPATranscript> getTargetSyllables() {
		return targetSylls;
	}

	public void setTargetSyllables(List<IPATranscript> targetSylls) {
		this.targetSylls = targetSylls;
	}

	public List<IPATranscript> getActualSyllables() {
		return actualSylls;
	}

	public void setActualSyllables(List<IPATranscript> actualSylls) {
		this.actualSylls = actualSylls;
	}

	public IPATranscript getTargetRep() {
		return targetRep;
	}

	public void setTargetRep(IPATranscript targetRep) {
		this.targetRep = targetRep;
	}

	public IPATranscript getActualRep() {
		return actualRep;
	}

	public void setActualRep(IPATranscript actualRep) {
		this.actualRep = actualRep;
	}

	private IPATranscript syllableContainingElement(IPAElement ele) {
		for(IPATranscript syll:getTargetSyllables()) {
			if(syll.indexOf(ele) >= 0)
				return syll;
		}

		for(IPATranscript syll:getActualSyllables()) {
			if(syll.indexOf(ele) >= 0)
				return syll;
		}

		return null;
	}

	private SyllableStress stressForElement(IPAElement ele) {
		final IPATranscript syll = syllableContainingElement(ele);
		return (syll != null ? syll.getExtension(SyllableStress.class) : null);
	}

	/**
	 * Calculate phone alignment.
	 *
	 * Method keep for API compatibility with older plug-ins/scripts.
	 *
	 * @deprecated
	 * @param ipaTarget
	 * @param ipaActual
	 * @return
	 */
	public PhoneMap calculatePhoneMap(IPATranscript ipaTarget, IPATranscript ipaActual) {
		return calculatePhoneAlignment(ipaTarget, ipaActual);
	}

	/**
	 * Calculate phone alignment
	 *
	 * @param ipaTarget
	 * @param ipaActual
	 * @return
	 */
	public PhoneMap calculatePhoneAlignment(IPATranscript ipaTarget, IPATranscript ipaActual) {
		setTargetRep(ipaTarget);
		setActualRep(ipaActual);

		setTargetSyllables(ipaTarget.syllables());
		setActualSyllables(ipaActual.syllables());

		for(IPATranscript syll:getTargetSyllables()) {
			final SyllableStress stress = syll.getExtension(SyllableStress.class);
			if(stress == SyllableStress.PrimaryStress || stress == SyllableStress.SecondaryStress) {
				hasStressedSyllables = true;
				break;
			}
		}

		final IPATranscript targetPhones = ipaTarget.audiblePhones();
		final IPATranscript actualPhones = ipaActual.audiblePhones();

		final IPAElement targetEles[] = new IPAElement[targetPhones.length()];
		for(int i = 0; i < targetPhones.length(); i++) targetEles[i] = targetPhones.elementAt(i);

		final IPAElement actualEles[] = new IPAElement[actualPhones.length()];
		for(int i = 0; i < actualPhones.length(); i++) actualEles[i] = actualPhones.elementAt(i);

		final AlignmentMap<IPAElement> alignment = calculateAlignment(targetEles, actualEles);

		// sort mappings
		Integer[] topAlignment = alignment.getTopAlignment();
		Integer[] bottomAlignment = alignment.getBottomAlignment();

		final PhoneMap retVal = new PhoneMap(ipaTarget, ipaActual);
		retVal.setTopElements(targetEles);
		retVal.setBottomElements(actualEles);
		retVal.setTopAlignment(topAlignment);
		retVal.setBottomAlignment(bottomAlignment);

		return retVal;
	}

}
