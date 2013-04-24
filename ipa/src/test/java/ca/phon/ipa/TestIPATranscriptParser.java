package ca.phon.ipa;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.features.FeatureMatrix;

/**
 * Test methods for the ipa parser.
 *
 */
@RunWith(JUnit4.class)
public class TestIPATranscriptParser {
	
	/**
	 * Test each individual consonant and vowel.
	 * 
	 */
	@Test
	public void testIndividualPhones() throws ParseException {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final List<Character> testChars = new ArrayList<Character>();
		testChars.addAll(fm.getCharactersWithFeature("Consonant"));
		testChars.addAll(fm.getCharactersWithFeature("Vowel"));
		
		for(Character c:testChars) {
			final String testString = "" + c;
			final IPATranscript transcript = IPATranscript.parseTranscript(testString);
			if(transcript.size() < 1) {
				System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
			}
			Assert.assertEquals(1, transcript.size());
			
			final IPAElement ipaEle = transcript.get(0);
			Assert.assertEquals(Phone.class, ipaEle.getClass());
			
			final Phone p = (Phone)ipaEle;
			Assert.assertEquals(testString, p.getText());
		}
	}
	
	@Test
	public void testPrefixDiacritics() {
		
	}
	
	@Test
	public void textSuffixDiacritics() {
		
	}
	
	@Test
	public void textReversedDiacritics() {
		
	}
	
	@Test
	public void textCombinedDiacritics() {
		
	}

}
