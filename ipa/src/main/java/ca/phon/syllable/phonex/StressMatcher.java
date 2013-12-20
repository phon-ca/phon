package ca.phon.syllable.phonex;

import java.util.HashSet;
import java.util.Set;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableStress;

/**
 * 
 */
public class StressMatcher implements PhoneMatcher {
	
	/**
	 * List of stress types allowed
	 */
	private Set<SyllableStress> stressTypes = 
			new HashSet<SyllableStress>();
	
	/**
	 * Add the given stress type to the list of
	 * allowed types.
	 * 
	 * @param type
	 */
	public void addType(SyllableStress type) {
		stressTypes.add(type);
	}

	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = false;
		
		SyllabificationInfo info = 
				p.getExtension(SyllabificationInfo.class);
		if(info != null) {
			SyllableStress phoneStress = info.getStress();
			retVal = stressTypes.contains(phoneStress);
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return (stressTypes.size() == SyllableStress.values().length);
	}

}
