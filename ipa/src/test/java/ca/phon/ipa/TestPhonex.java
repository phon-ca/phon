package ca.phon.ipa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.phonex.NoSuchPluginException;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

@RunWith(JUnit4.class)
public class TestPhonex {

	@Test(expected=NoSuchPluginException.class)
	public void testMissingPlugin() throws Exception {
		final String phonex = "\\v:noplugin()";
		PhonexPattern.compile(phonex);
	}
	
	@Test
	public void testDotText() throws Exception {
		final String phonex = "(\\c*\\v+\\c*)*";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "bbaab aba";
		
		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseTranscript(ipa));
		while(matcher.find()) {
			System.out.println(matcher.group());
		}
	}
}
