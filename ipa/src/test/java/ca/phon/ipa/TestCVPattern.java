package ca.phon.ipa;

import java.text.ParseException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestCVPattern {

	@Test
	public void testContains() throws ParseException {
		final String txt = "ˈk:Oə:Nn:Cˌs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final String pattern = "CV+";
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		Assert.assertEquals(true, ipa.containsCVPattern(pattern));
	}
	
	@Test
	public void testMatches() throws ParseException {
		final String txt = "ˈk:Oə:Nn:Cˌs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final String pattern = "CA+C";
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		Assert.assertEquals(true, ipa.matchesCVPattern(pattern));
	}

	@Test
	public void testFind() throws ParseException {
		final String txt = "ˈk:Oə:Nn:Cˌs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final String pattern = "CV+";
		final String[] expected = { "kə", "ɹeɪ" };
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		final List<IPATranscript> found = ipa.findCVPattern(pattern);
		
		Assert.assertEquals(expected.length, found.size());
		for(int i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], found.get(i).toString());
		}
	}
	
}
