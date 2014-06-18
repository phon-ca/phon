package ca.phon.ipa;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;

@RunWith(JUnit4.class)
public class TestIPAParserErrors {

	@Test
	public void testStrayLongDiacritic() throws ParseException {
		final String txt = "ɔːːːː";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		// expected to recover by reducing to just the vowel
		Assert.assertEquals(1, ipa.length());
		Assert.assertEquals("ɔ", ipa.toString());
	}
	
}
