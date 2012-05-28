package ca.phon.syllabifier.phonex;

import ca.phon.ipa.phone.phonex.PhoneMatcher;
import ca.phon.ipa.phone.phonex.PhonexPatternException;
import ca.phon.ipa.phone.phonex.PhonexPlugin;
import ca.phon.ipa.phone.phonex.PluginMatcher;
import ca.phon.syllable.SyllableConstituentType;

/**
 * <p>Phonex plug-in for syllable constituent type matching.  This
 * matcher accepts a list of syllable constituent types separated
 * by the '|' symbol. (e.g., {}:sctype("O|LA") - onset or leftappendix).  Both
 * long and short contituent type identifiers can be used.  For the
 * list of contituent types, see {@link SyllableConstituentType}.</p>
 * 
 * <p>This is the default type matcher in Phonex and can also be written
 * without the plug-in matcher identifier.  E.g., <code>{}:O|LA == {}:sctype("O|LA")</code>.
 * </p>
 */
@PhonexPlugin("sctype")
public class SyllableConstituentPhonexPlugin implements PluginMatcher {
	
	@Override
	public PhoneMatcher createMatcher(String input)
			throws PhonexPatternException {
		SyllabificationInfoMatcher retVal = new SyllabificationInfoMatcher();
		String[] scTypes = input.split("\\|");
		for(String scTypeId:scTypes) {
			boolean not = (scTypeId.startsWith("-") ? true : false);
			if(not)
				scTypeId = scTypeId.substring(1);
			SyllableConstituentType scType = SyllableConstituentType.fromString(scTypeId);
			if(scType == null) 
				throw new PhonexPatternException("Invalid syllable constituent type '" + 
						scTypeId + "'");
			else {
				if(not)
					retVal.getDisallowedTypes().add(scType);
				else
					retVal.getAllowedTypes().add(scType);
			}
		}
		return retVal;
	}

}
