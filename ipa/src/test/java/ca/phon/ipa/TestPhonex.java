package ca.phon.ipa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

@RunWith(JUnit4.class)
public class TestPhonex {

	@Test
	public void testPhonex() throws Exception {
		final String ipa = "ˈbʌɹθˌd";
		final String phonex = "\\c*\\v+";
		
		final PhonexPattern pattern = PhonexPattern.compile("{}:Nucleus");
		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseTranscript(ipa));
		int cIdx = 0;
		while(matcher.find()) {
			System.out.println(matcher.group());
		}
	}
	
}
