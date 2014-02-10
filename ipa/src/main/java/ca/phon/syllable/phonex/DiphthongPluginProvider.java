package ca.phon.syllable.phonex;

import java.util.List;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

/**
 * Provides the 'diphthong' phonex plug-in matcher. 
 */
@PhonexPlugin(name="diphthong")
public class DiphthongPluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		boolean diphthong = true;
		if(args.size() == 1) {
			diphthong = Boolean.parseBoolean(args.get(0));
		}
		return new DiphthongMatcher(diphthong);
	}

}
