package ca.phon.ipa.phone.phonex.plugins;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.phone.phonex.PhoneMatcher;
import ca.phon.ipa.phone.phonex.PhonexPatternException;
import ca.phon.ipa.phone.phonex.PhonexPlugin;
import ca.phon.ipa.phone.phonex.PluginMatcher;


/**
 * Provides a phonex plug-in for matching the 
 * prefix diacritic section of a phone.
 *
 */
@PhonexPlugin("prefix")
public class PrefixDiacriticPhonexPlugin implements PluginMatcher {

	@Override
	public PhoneMatcher createMatcher(String input)
			throws PhonexPatternException {
		if(input.indexOf("_") >= 0) {
			return createCompoundMatcher(input);
		} else {
			return createBasicMatcher(input);
		}
	}

	private PhoneMatcher createBasicMatcher(String input) {
		final List<Character> allowed = new ArrayList<Character>();
		final List<Character> forbidden = new ArrayList<Character>();
		parseDiacriticExpr(input, allowed, forbidden);
		return new CombiningDiacriticPhoneMatcher(allowed, forbidden);
	}

	private PhoneMatcher createCompoundMatcher(String input) {
		PhoneMatcher retVal = null;
		String[] cmpExprs = input.split("_");
		if(cmpExprs.length == 2) {
			PhoneMatcher p1Matcher = createBasicMatcher(cmpExprs[0]);
			PhoneMatcher p2Matcher = createBasicMatcher(cmpExprs[1]);
			retVal = new CompoundDiacriticPhoneMatcher(p1Matcher, p2Matcher);
		}
		return retVal;
	}
	
	private void parseDiacriticExpr(String expr, List<Character> allowed, List<Character> forbidden) {
		boolean isForbidden = false;
		for(Character c:expr.toCharArray()) {
			if(!Character.isWhitespace(c)) {
				if(c == '-') {
					isForbidden = true;
				} else {
					if(isForbidden)
						forbidden.add(c);
					else
						allowed.add(c);
				}
			}
		}
	}
}
