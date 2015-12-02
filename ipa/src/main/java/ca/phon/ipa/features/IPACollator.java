package ca.phon.ipa.features;

import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Set;

/**
 * Text collation for IPA transcriptions.  The ordering used is the same
 * ordering found in the <code>features.xml</code> file.
 */
public class IPACollator extends RuleBasedCollator {

	private static String rules = null;
	
	private static final String createRules() {
		if(rules == null) {final Set<Character> chars = FeatureMatrix.getInstance().getCharacterSet();
			final StringBuffer buffer = new StringBuffer();
			chars.forEach( (c) -> buffer.append(" < ").append(c) );
			rules = buffer.toString();
		}
		return rules;
	}
	
	public IPACollator() throws ParseException {
		super(createRules());
	}
	
}
