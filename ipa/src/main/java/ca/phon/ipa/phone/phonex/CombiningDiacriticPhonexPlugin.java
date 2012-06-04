package ca.phon.ipa.phone.phonex;

import java.util.ArrayList;
import java.util.List;


/**
 * Provides a phonex plug-in for matching the 
 * combining diacritic section of a phone.
 *
 */
@PhonexPlugin("comb")
public class CombiningDiacriticPhonexPlugin implements PluginMatcher {

	@Override
	public PhoneMatcher createMatcher(String input)
			throws PhonexPatternException {
		final List<Character> combingChars = 
				new ArrayList<Character>();
		for(Character c:input.toCharArray()) {
			combingChars.add(c);
		}
		return new CombiningDiacriticPhoneMatcher(combingChars);
	}

}
