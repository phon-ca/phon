package ca.phon.phonex.plugins;

import java.text.ParseException;
import java.util.List;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

/**
 * Provides the 'len' plug-in matcher.
 *
 */
@PhonexPlugin(name="len")
public class LengthPluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		final LengthMatcher matcher = new LengthMatcher();
		if(args.size() == 1) {
			final String txt = args.get(0);
			try {
				matcher.setLength(Float.parseFloat(txt));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return matcher;
	}

}
