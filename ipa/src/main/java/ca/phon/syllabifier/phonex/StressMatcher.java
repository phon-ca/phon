package ca.phon.syllabifier.phonex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.phonex.PhoneMatcher;
import ca.phon.syllabifier.SyllabificationInfo;
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
	public boolean matches(Phone p) {
		boolean retVal = false;
		
		SyllabificationInfo info = 
				p.getCapability(SyllabificationInfo.class);
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
