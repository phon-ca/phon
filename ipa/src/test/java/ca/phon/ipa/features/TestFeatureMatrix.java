package ca.phon.ipa.features;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;

@RunWith(JUnit4.class)
public class TestFeatureMatrix {

	private static final Logger LOGGER = Logger
			.getLogger(TestFeatureMatrix.class.getName());
	
	/**
	 * Ensures that all phones defined in the {@link FeatureMatrix}
	 * are supported by the IPA parser.
	 */
	@Test
	public void ensureMatrixFullySupported() {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Set<Character> fmSet = new TreeSet<Character>(fm.getCharacterSet());
		final Set<Character> tokenSet = new TreeSet<Character>(IPATokens.getSharedInstance().getCharacterSet());
		
		// remove all supported characters
		fmSet.removeAll(tokenSet);
	
		for(Character c:fmSet) {
			LOGGER.severe("Character '\\u" + Integer.toHexString((int)c) + "' with feature set {" + 
					fm.getFeatureSet(c) + "} is not supported by the IPA parser.");
		}
		Assert.assertEquals(0, fmSet.size());
	}
	
}
