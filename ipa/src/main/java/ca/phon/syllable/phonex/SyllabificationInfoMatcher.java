package ca.phon.syllable.phonex;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.phonex.PhoneMatcher;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Syllable constituent type matcher for phonex.
 */
public final class SyllabificationInfoMatcher implements PhoneMatcher {
	
	/**
	 * Allowed constituent types
	 */
	private final List<SyllableConstituentType> allowedTypes = 
			new ArrayList<SyllableConstituentType>();

	/**
	 * Dis-allowed constituent types
	 */
	private final List<SyllableConstituentType> disallowedTypes = 
			new ArrayList<SyllableConstituentType>();
	
	/**
	 * Constructor
	 */
	public SyllabificationInfoMatcher() {
		
	}
	
	/**
	 * Access to the allowed types list
	 */
	public List<SyllableConstituentType> getAllowedTypes() {
		return this.allowedTypes;
	}
	
	/**
	 * Access to the disallowed types list
	 */
	public List<SyllableConstituentType> getDisallowedTypes() {
		return this.disallowedTypes;
	}
	
	@Override
	public boolean matches(Phone p) {
		if(matchesAnything()) return true;
		
		boolean retVal = true;
		SyllabificationInfo scInfo = p.getCapability(SyllabificationInfo.class);
		if(scInfo != null) {
			if(allowedTypes.size() > 0)
				retVal &= allowedTypes.contains(scInfo.getConstituentType());
			if(disallowedTypes.size() > 0)
				retVal &= !disallowedTypes.contains(scInfo.getConstituentType());
		} else {
			retVal = false;
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return (allowedTypes.size() == 0 && disallowedTypes.size() == 0);
	}

	@Override
	public String toString() {
		String retVal = "";
		
		for(SyllableConstituentType scType:allowedTypes) {
			retVal += (retVal.length() > 0 ? "|":"") + scType.getIdentifier();
		}
		for(SyllableConstituentType scType:disallowedTypes) {
			retVal += (retVal.length() > 0 ? "|":"") + "-" + scType.getIdentifier();
		}
		
		return retVal;
	}
}
