package ca.phon.syllabifier.phonex;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

/**
 * Test sonority distance phonex plug-ins.
 *
 */
@RunWith(JUnit4.class)
public class TestMdcPlugin {

	@Test
	public void testMDCPlugin() throws Exception {
		final String text = "ktki";
		final String phonex = "(\\c)(\\c:mdc('0,true'))";
		final IPATranscript ipa = IPATranscript.parseTranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals(ipa.subList(0, 2), matcher.group());
	}
	
}
