package ca.phon.syllable.phonex;

import ca.phon.ipa.phone.phonex.PhoneMatcher;
import ca.phon.ipa.phone.phonex.PhonexPatternException;
import ca.phon.ipa.phone.phonex.PhonexPlugin;
import ca.phon.ipa.phone.phonex.PluginMatcher;
import ca.phon.syllable.SyllableStress;

/**
 * 
 */
@PhonexPlugin("stress")
public class StressPhonexPlugin implements PluginMatcher {

	@Override
	public PhoneMatcher createMatcher(String input)
			throws PhonexPatternException {
		StressMatcher retVal = new StressMatcher();
		
		String[] types = input.split("\\|");
		for(String typeString:types) {
			SyllableStress stress = SyllableStress.fromString(typeString);
			if(stress != null)
				retVal.addType(stress);
		}
		
		return retVal;
	}

}
