package ca.phon.syllable.phonex;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.phone.phonex.PhoneMatcher;
import ca.phon.ipa.phone.phonex.PhonexPatternException;
import ca.phon.ipa.phone.phonex.PhonexPlugin;
import ca.phon.ipa.phone.phonex.PluginMatcher;

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
