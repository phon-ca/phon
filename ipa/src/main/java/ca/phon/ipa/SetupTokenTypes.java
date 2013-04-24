package ca.phon.ipa;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;

public class SetupTokenTypes {
	
	public static void main(String[] args) throws Exception {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final IPATokens ipaTokens = IPATokens.getSharedInstance();
		
		final List<Character> missingChars = new ArrayList<Character>();
		final List<Character> extraChars = new ArrayList<Character>();
		
		for(Character c:fm.getCharacterSet()) {
			final IPATokenType tt = ipaTokens.getTokenType(c);
			if(tt == null) {
				missingChars.add(c);
			}
		}
		
	}

}
