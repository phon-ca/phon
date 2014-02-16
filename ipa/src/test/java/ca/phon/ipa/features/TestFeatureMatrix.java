package ca.phon.ipa.features;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
	@Ignore
	@Test
	public void ensureMatrixFullySupported() throws Exception {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Set<Character> fmSet = new TreeSet<Character>(fm.getCharacterSet());
		final Set<Character> tokenSet = new TreeSet<Character>(IPATokens.getSharedInstance().getCharacterSet());
		
		// remove all supported characters
		fmSet.removeAll(tokenSet);
	
		final ObjectFactory ipaFactory = new ObjectFactory();
		final JAXBContext context = JAXBContext.newInstance(ipaFactory.getClass());
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		
		for(Character c:fmSet) {
//			LOGGER.severe("Character '" + c + " ' (\\u" + Integer.toHexString((int)c) + ") with feature set {" + 
//					fm.getFeatureSet(c) + "} is not supported by the IPA parser.");
			final FeatureSet fs = fm.getFeatureSet(c);
			final CharType charType = ipaFactory.createCharType();
			charType.setName(new String());
			charType.setValue(c+"");
			TokenType tokenType = null;
		
			
			if(fs.hasFeature("consonant")) {
				tokenType = TokenType.CONSONANT;
			} else if(fs.hasFeature("vowel")) {
				tokenType = TokenType.VOWEL;
			} else if(fs.hasFeature("diacritic")) {
				tokenType = TokenType.PREFIX_DIACRITIC;
			}
			charType.setToken(tokenType);
			
//			marshaller.marshal(ipaFactory.createChar(charType), System.out);
			System.out.println("\"" + c + "\",\"" + fs.toString() + "\",\"\\u" + Integer.toHexString((int)c) + "\"");
		}
//		Assert.assertEquals(0, fmSet.size());
	}
	
	@Ignore
	@Test
	public void ensureIpaFullySupported() {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Set<Character> fmSet = new TreeSet<Character>(fm.getCharacterSet());
		final Set<Character> tokenSet = new TreeSet<Character>(IPATokens.getSharedInstance().getCharacterSet());
		
		// remove all supported characters
		tokenSet.removeAll(fmSet);
	
		for(Character c:tokenSet) {
			LOGGER.severe("Character '" + c + "' (\\u" + Integer.toHexString((int)c) + ") is not supported by the FeatureMatrix.");
		}
//		Assert.assertEquals(0, tokenSet.size());
	}
}
