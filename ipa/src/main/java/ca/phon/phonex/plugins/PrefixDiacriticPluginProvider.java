package ca.phon.phonex.plugins;

import java.util.List;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

/**
 * <p>Provides a matcher for prefix diacritics.  Takes a single
 * argument which is a string list of allowed/forbidden diacritics.
 * Forbidden diacritics are prefixed with '-'.</p>
 * 
 * <p>Usage: <code>comb("&lt;list of diacritics&gt;")</code></br/>
 * E.g., Look for a consonant that has the '&#2045;' diacritic but <em>not</em>
 * the '&#2048;' diacritic.</br>
 * <pre>
 * \c:prefix("&#2045;-&#2048;")
 * </pre>
 * 
 * </p>
 *
 */
@PhonexPlugin(name="prefix", requiredArgs={String.class})
public class PrefixDiacriticPluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		if(args == null)
			throw new NullPointerException();
		if(args.size() != 1) {
			throw new IllegalArgumentException();
		}
		
		final String arg = args.get(0);
		return new PrefixDiacriticPhoneMatcher(arg);
	}

}
