package ca.phon.phonex;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;

/**
 * Test phonex boundary detection.
 */
@RunWith(JUnit4.class)
public class TestBoundaries {

	@Test
	public void testBeginningOfInput() throws ParseException {
		final String text = "assassin";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		final String phonex = "^ass";
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		int numFound = 0;
		while(matcher.find()) numFound++;
		
		Assert.assertEquals(1, numFound);
	}
	
	@Test
	public void testEndOfInput() throws ParseException {
		final String text = "babababa";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		final String phonex = "ba$";
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		int numFound = 0;
		while(matcher.find()) numFound++;
		
		Assert.assertEquals(1, numFound);
	}
	
	@Test
	public void testWordBoundary() throws ParseException {
		final String text = "hello world";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		final String phonex = "\\b\\c.*\\v\\b";
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals(ipa.subList(0, 6), matcher.group());
	}
	
	
	@Test
	public void testSyllableBoundaries() throws ParseException {
		final String text = "b:Oa:N\u02c8b:Oa:Nb:Oa:N";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
	
		final String phonex1 = "\\Sba";
		final PhonexPattern p1 = PhonexPattern.compile(phonex1);
		final PhonexMatcher m1 = p1.matcher(ipa);
		
		int numFound = 0;
		while(m1.find()) numFound++;
		Assert.assertEquals(3, numFound);
		
		final String phonex2 = "ba\\S";
		final PhonexPattern p2 = PhonexPattern.compile(phonex2);
		final PhonexMatcher m2 = p2.matcher(ipa);
		
		numFound = 0;
		while(m2.find()) numFound++;
		Assert.assertEquals(3, numFound);
	}
}
