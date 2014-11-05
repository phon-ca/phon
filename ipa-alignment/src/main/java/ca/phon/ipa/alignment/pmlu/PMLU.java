package ca.phon.ipa.alignment.pmlu;

import java.lang.ref.WeakReference;

import ca.phon.extensions.Extension;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

/**
 * Implements "Phonological Mean Length of Utterance" (Ingram 2002)
 * calculation for {@link PhoneMap}s.
 */
@Extension(PhoneMap.class)
public class PMLU {

	private final WeakReference<PhoneMap> mapRef;
	
	public PMLU(PhoneMap phoneMap) {
		this.mapRef = new WeakReference<PhoneMap>(phoneMap);
	}
	
	public PhoneMap getPhoneMap() {
		return mapRef.get();
	}
	
	/**
	 * Calculate target PMLU. PMLU is calculated as the number of segments
	 * in the target transcript plus the number of consonants.
	 * 
	 * @return target PMLU
	 */
	public int targetPMLU() {
		final PhoneMap pm = getPhoneMap();
		final IPATranscript target = pm.getTargetRep();
		
		int pmlu = target.removePunctuation().length();
		final PhonexPattern pattern = PhonexPattern.compile("\\c");
		final PhonexMatcher matcher = pattern.matcher(target);
		while(matcher.find()) ++pmlu;
		
		return pmlu;
	}
	
	/**
	 * Calculate actual PMLU. PMLU is calculated as the number of segments
	 * in the actual transcript plus number of target-like consonants.
	 * 
	 * @return actual PMLU
	 */
	public int actualPMLU() {
		final PhoneMap pm = getPhoneMap();
		
		int pmlu = 0;
		for(int i = 0; i < pm.getAlignmentLength(); i++) {
			final IPAElement targetEle = pm.getTopAlignmentElements().get(i);
			final IPAElement actualEle = pm.getBottomAlignmentElements().get(i);
			
			if(targetEle != null) {
				// we have a target element...
				if(actualEle != null) {
					// increment pmlu since we have a valid segment
					++pmlu;
				} else {
					continue;
				}
				if(targetEle.getFeatureSet().hasFeature("Consonant")) {
					// check for target-like conditions, in this case target-like means
					// both base glyphs match
					if(actualEle.getFeatureSet().hasFeature("Consonant")) {
						final Phone tPhone = (Phone)targetEle;
						final Phone aPhone = (Phone)actualEle;
						
						// we have a target-like consonant, increment pmlu
						if(tPhone.getBase().equals(aPhone.getBase())) {
							++pmlu;
						}
					}
				}
			}
			// added segments are ignored...
		}
		
		return pmlu;
	}
	
	/**
	 * Calculate PWP = actualPMLU()/targetPMLU()
	 * 
	 * @return pwp
	 */
	public float PWP() {
		return (float)actualPMLU()/(float)targetPMLU();
	}
	
}
