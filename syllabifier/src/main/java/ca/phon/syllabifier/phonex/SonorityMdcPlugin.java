package ca.phon.syllabifier.phonex;

import java.text.ParseException;
import java.util.List;

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
@PhonexPlugin(name = "mdc")
public class SonorityMdcPlugin
implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		if(args.size() != 2)
			throw new IllegalArgumentException("Invalid number of arguments, should be 2, is " + args.size());
		int dist = 0;
		boolean allowFlat = false;
		try {
			dist = Integer.parseInt(args.get(0));
			allowFlat = Boolean.parseBoolean(args.get(1));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
		
		return new SonorityDistancePhoneMatcher(dist, allowFlat);
	}

}
