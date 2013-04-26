package ca.phon.syllabifier.phonex;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPatternException;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

/**
 * Provides the phonex 'mdc' (minimum distance constraint)
 * plug-in for syllabification.  Format of input should be
 * 
 *  INT(,[true|false])?
 *  
 * Where INT is the minimum distance from the previous phone
 * and the optional boolean indicates if flat sonority is
 * allowed.
 */
@PhonexPlugin("mdc")
public class SonorityMdcPlugin implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(String input)
			throws PhonexPatternException {
		boolean allowFlat = false;
		int distance = 0;
		
		try {
			if(input.indexOf(',') > 0) {
				final String[] values = input.split(",");
				distance = Integer.parseInt(values[0]);
				allowFlat = Boolean.parseBoolean(values[1]);
			} else {
				distance = Integer.parseInt(input);
			}
		} catch (NumberFormatException e) {
			throw new PhonexPatternException(e);
		}
		
		return new SonorityDistancePhoneMatcher(distance, allowFlat);
	}

}
