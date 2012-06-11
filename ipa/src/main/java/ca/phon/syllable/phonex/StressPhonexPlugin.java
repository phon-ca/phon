package ca.phon.syllable.phonex;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPatternException;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;
import ca.phon.syllable.SyllableStress;

/**
 * 
 */
@PhonexPlugin("stress")
public class StressPhonexPlugin implements PluginProvider {

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
