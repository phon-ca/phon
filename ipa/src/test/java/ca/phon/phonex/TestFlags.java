/*
 * 
 */
package ca.phon.phonex;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import junit.framework.Assert;

@RunWith(JUnit4.class)
public class TestFlags {

	@Test
	public void testOverlappingMatches() throws Exception {
		final String phonex = "\\c\\v\\c\\v/o";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "badafagabadafa";

		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseIPATranscript(ipa));
		int numMatches = 0;
		while(matcher.find()) {
			++numMatches;
		}
		Assert.assertEquals(6, numMatches);
	}

	@Test(expected=PhonexPatternException.class)
	public void testInvalidFlag() throws Exception {
		final String phonex = "\\c\\v/z";
		PhonexPattern.compile(phonex);
	}

}
