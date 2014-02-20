package ca.phon.ipa.features;

import java.text.NumberFormat;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.text.NumberFormatter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import junit.framework.Assert;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;
import ca.phon.ipa.xml.CharType;
import ca.phon.ipa.xml.ObjectFactory;
import ca.phon.ipa.xml.TokenType;

@RunWith(JUnit4.class)
public class TestFeatureMatrix {

	private static final Logger LOGGER = Logger
			.getLogger(TestFeatureMatrix.class.getName());
	
	/**
	 * Ensures that all phones defined in the {@link FeatureMatrix}
	 * are supported by the IPA parser.
	 */
	@Test
	public void ensureMatrixFullySupported() throws Exception {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Set<Character> fmSet = new TreeSet<Character>(fm.getCharacterSet());
		final Set<Character> tokenSet = new TreeSet<Character>(IPATokens.getSharedInstance().getCharacterSet());
		
		// remove all supported characters
		fmSet.removeAll(tokenSet);
		Assert.assertEquals(0, fmSet.size());
	}
	
	@Test
	public void ensureIpaFullySupported() {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Set<Character> fmSet = new TreeSet<Character>(fm.getCharacterSet());
		final Set<Character> tokenSet = new TreeSet<Character>(IPATokens.getSharedInstance().getCharacterSet());
		
		// remove all supported characters
		tokenSet.removeAll(fmSet);
		Assert.assertEquals(0, tokenSet.size());
	}
}
