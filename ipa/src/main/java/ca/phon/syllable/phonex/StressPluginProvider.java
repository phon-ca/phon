package ca.phon.syllable.phonex;

import java.util.List;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;
import ca.phon.syllable.SyllableStress;

/**
 * <p>Provides the plug-in matcher for syllable stress. Stress type
 * is identified by the following list:<br/>
 * 
 * <ul>
 *	<li>U - No Stress</li>
 *  <li>1 - Primary Stress</li>
 *  <li>2 - Secondary Stress</li>
 * </ul>
 * 
 * E.g., Search for unstressed consonants</br>
 * <pre>
 * \c:stress("U")
 * </pre>
 * 
 * Stress types may also be 'or'-ed using the pipe ('|') symbol.</br>
 * 
 * E.g., Search for stressed (primary or secondary) consonants</br>
 * <pre>
 * \c:stress("1|2")
 * </pre>
 * </p>
 */
@PhonexPlugin(name = "stress", requiredArgs={String.class})
public class StressPluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		if(args == null)
			throw new NullPointerException();
		if(args.size() != 1) {
			throw new IllegalArgumentException();
		}
		
		final String arg = args.get(0);
		StressMatcher retVal = new StressMatcher();
		
		String[] types = arg.split("\\|");
		for(String typeString:types) {
			SyllableStress stress = SyllableStress.fromString(typeString);
			if(stress != null)
				retVal.addType(stress);
			else 
				throw new IllegalArgumentException("Invalid stress type: " + typeString);
		}
		
		return retVal;
	}

}
