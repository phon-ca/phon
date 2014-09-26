package ca.phon.phonex;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;

/**
 * Test basic phonex constructs
 *
 */
@RunWith(JUnit4.class)
public class TestBasicConstructs {

	@Test
	public void testSinglePhoneMatcher() throws ParseException {
		// create a test string 
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		String text = new String();
		final FeatureSet testFeatures = FeatureSet.fromArray(new String[]{"Consonant", "Obs"});
		
		for(Character c:fm.getCharacterSet()) {
			if(fm.getFeatureSet(c).intersect(testFeatures).equals(testFeatures))
				text += c;
		}
		
		// ensure that phonex finds the character in the correct position
		final IPATranscript bigIpa = IPATranscript.parseIPATranscript(text);
		for(int i = 0; i < text.length(); i++) {
			final Character c = text.charAt(i);
			Assert.assertEquals(i, bigIpa.indexOf(c + ""));
		}
	}

	@Test
	public void testCompoundPhoneMatcher() throws ParseException {
		final String text = "st\u0361\u0283aad\u035c\u0292";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		// look for all compound phones
		final String phonex = "._.";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		int numFound = 0;
		while(matcher.find()) numFound++;
		Assert.assertEquals(2, numFound);
		
		// look for specific compound phones
	}
	
	@Test
	public void testRegexMatcher() throws ParseException {
		final String text =  "st\u0361\u0283aad\u035c\u0292";
		final String phonex = "'t.+'";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.find());
	}
	
	@Test
	public void testFeatureSetMatcher() throws ParseException {
		
	}

	@Test
	public void testUnicodeValueMatcher() throws ParseException {
		
	}
	
	@Test
	public void testAlignmentMarker() throws ParseException {
		final String text = "ba \u2194 ab";
		final String phonex = "(\\w+)\\b\u2194\\b(\\w+)";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		System.out.println(ipa);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.matches());
		Assert.assertEquals(ipa.subsection(0, 2).toList(), matcher.group(1));
		Assert.assertEquals(ipa.subsection(5, 7).toList(), matcher.group(2));
	}
	
}
