package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Dictionary capability for generating ipa
 * suggestions. Suggestions are not validated
 * and may be very inaccurate - use at own risk
 *
 */
@Extension(IPADictionary.class)
public interface GenerateSuggestions {

	/**
	 * Generate a list of suggestions for a given
	 * orthography.  If the given orthography appears in the 
	 * dictionary as-is this method returns the same
	 * as lookup.
	 * 
	 * @param orthography
	 * @return a list of generated ipa suggestions
	 * 
	 */
	public String[] generateSuggestions(String orthography);
	
}
