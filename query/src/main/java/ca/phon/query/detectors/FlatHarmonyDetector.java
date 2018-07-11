package ca.phon.query.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.PhoneticProfile;
import ca.phon.util.Tuple;

/**
 * Detects harmony on single {@link IPATranscript}s. This is useful for
 * babbling data.
 * 
 */
public class FlatHarmonyDetector {

	/**
	 * Detect consonant harmony within the given ipa
	 * 
	 * @return list of harmony locations
	 */
	public List<Tuple<Integer, Integer>> detectConsonantHarmony(IPATranscript ipa) {
		final List<Tuple<Integer, Integer>> retVal = new ArrayList<>();
		
		final List<IPAElement> consonants = 
				StreamSupport.stream(ipa.spliterator(), false)
					.filter( (e) -> e.getFeatureSet().hasFeature("consonant") )
					.collect(Collectors.toList());
		
		for(int i = 0; i < consonants.size()-1; i++) {
			IPAElement ele1 = consonants.get(i);
			for(int j = i+1; j < consonants.size(); j++) {
				IPAElement ele2 = consonants.get(j);
				if(isConsonantHarmony(ele1, ele2)) {
					Tuple<Integer, Integer> pair = new Tuple<>(ipa.indexOf(ele1), ipa.indexOf(ele2));
					retVal.add(pair);
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Detect vowel harmony within the given ipa
	 * 
	 * @param ipa
	 * 
	 * @return list of harmony locations
	 */
	public List<Tuple<Integer, Integer>> detectVowelHarmony(IPATranscript ipa) {
		final List<Tuple<Integer, Integer>> retVal = new ArrayList<>();
		
		final List<IPAElement> vowels = 
				StreamSupport.stream(ipa.spliterator(), false)
					.filter( (e) -> e.getFeatureSet().hasFeature("vowel") )
					.collect(Collectors.toList());
		
		for(int i = 0; i < vowels.size()-1; i++) {
			IPAElement ele1 = vowels.get(i);
			for(int j = i+1; j < vowels.size(); j++) {
				IPAElement ele2 = vowels.get(j);
				if(isVowelHarmony(ele1, ele2)) {
					Tuple<Integer, Integer> pair = new Tuple<>(ipa.indexOf(ele1), ipa.indexOf(ele2));
					retVal.add(pair);
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * A harmony exists between the two phones if both place and manner
	 * dimensions are the same.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public boolean isConsonantHarmony(IPAElement p1, IPAElement p2) {
		return checkHarmony(p1, p2, PhoneDimension.PLACE, PhoneDimension.MANNER);
	}
	
	public boolean isVowelHarmony(IPAElement ele1, IPAElement ele2) {
		return checkHarmony(ele1, ele2, PhoneDimension.HEIGHT, PhoneDimension.BACKNESS, PhoneDimension.TENSENESS);
	}
	
	public boolean checkHarmony(IPAElement ele1, IPAElement ele2, PhoneDimension ...dimensions) {
		boolean retVal = true;
		
		final PhoneticProfile profile1 = new PhoneticProfile(ele1);
		final PhoneticProfile profile2 = new PhoneticProfile(ele2);
		
		for(PhoneDimension dimension:dimensions) {
			retVal &= 
				(profile1.getProfile().get(dimension).equals(profile2.getProfile().get(dimension)));
		}
		
		return retVal;
	}
	
}
