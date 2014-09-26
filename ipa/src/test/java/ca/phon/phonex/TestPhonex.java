package ca.phon.phonex;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;

@RunWith(JUnit4.class)
public class TestPhonex {

	@Test(expected=NoSuchPluginException.class)
	public void testMissingPlugin() throws Exception {
		final String phonex = "\\v:noplugin()";
		PhonexPattern.compile(phonex);
	}
	
	@Test
	public void testBacktracking() throws Exception {
		final String phonex = "(\\c*\\v+\\c*)*";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "bbaab aba";
		
		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseIPATranscript(ipa));
		int numMatches = 0;
		while(matcher.find()) {
			++numMatches;
		}
		
		Assert.assertEquals(2, numMatches);
	}
}
