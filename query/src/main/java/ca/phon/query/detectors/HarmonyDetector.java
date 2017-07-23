package ca.phon.query.detectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneMap;

/**
 * 
 */
public class HarmonyDetector extends BasicHarmonyDetector {

	/* Consonants */
	final static String[] placeFeatures = { 
			"{labial, -dental}",
			"{labial, dental}", 
			"{dental}", 
			"{alveolar}", 
			"{alveopalatal}", 
			"{lateral}", 
			"{retroflex}", 
			"{palatal}",
			"{velar}", 
			"{uvular}",
			"{pharyngeal}", 
			"{laryngeal}" };

	final static String[] mannerFeatures = { 
			"{stop, -nasal}", 
			"{fricative}", 
			"{affricate}", 
			"{nasal}",
			"{liquid, lateral}", 
			"{rhotic}", 
			"{glide}", 
			"{vowel}" };

	final static String[] voicingFeatures = { 
			"{voiceless, -aspirated}", 
			"{voiceless, aspirated}", 
			"{voiced}" };

	private boolean includePlace = true;

	private boolean includeManner = true;

	private boolean includeVoicing = true;

	/* Vowels */
	final static String[] heightFeatures = { "{high}", "{mid}", "{low}" };
	final static String[] backnessFeatures = { "{front}", "{central}", "{back}" };
	final static String[] tensenessFeatures = { "{tense}", "{lax}" };
	final static String[] roundingFeatures = { "{round}", "{-round}" };

	private boolean includeHeight = true;
	
	private boolean includeBackness = true;
	
	private boolean includeTenseness = true;
	
	private boolean includeRounding = true;

	public HarmonyDetector(boolean consonants) {
		super(consonants);
	}
	
	/**
	 * Returns the first phonex expression in the given list
	 * to which the given {@link IPAElement} matches.
	 * 
	 * @param featurePhonex
	 * @param ele
	 * @return index of the matched phonex expression or -1 if no expression matches
	 */
	public int getFeatureCategory(String[] featurePhonex, IPAElement ele) {
		int retVal = -1;

		for(int i = 0; i < featurePhonex.length; i++) {
			final String features = featurePhonex[i];
			final IPATranscript transcript = (new IPATranscriptBuilder()).append(ele).toIPATranscript();
			if(transcript.matches(features)) {
				retVal = i;
				break;
			}
		}

		return retVal;
	}

	@Override
	public Collection<DetectorResult> detect(PhoneMap pm) {
		final Collection<DetectorResult> allPossibleResults = super.detect(pm);
		final List<DetectorResult> results = new ArrayList<>();
	
		for(DetectorResult possibleResult:allPossibleResults) {
			if(isConsonants()) {
				results.addAll(findConsonantHarmony(possibleResult));
			} else {
				results.addAll(findVowelHarmony(possibleResult));
			}
		}
		
		return results;
	}
	
	private List<DetectorResult> findConsonantHarmony(DetectorResult result) {
		return new ArrayList<>();
	}
	
	private List<DetectorResult> findVowelHarmony(DetectorResult result) {
		return new ArrayList<>();
	}
	
}
