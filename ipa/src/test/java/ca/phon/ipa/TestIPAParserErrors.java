package ca.phon.ipa;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;

@RunWith(JUnit4.class)
public class TestIPAParserErrors {

	@Test
	public void testStrayLongDiacritic() throws ParseException {
		final String txt = "ɔ\u02d0\u02d0\u02d0\u02d0";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		// expected to recover by reducing to just the vowel
		Assert.assertEquals(1, ipa.length());
		Assert.assertEquals("ɔ", ipa.toString());
	}
	
	@Test
	public void testStrayInitialLengthDiacritic() throws ParseException {
		final String txt = "ʷɔ\u02d0";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		Assert.assertEquals(2, ipa.length());
		Assert.assertEquals("Xɔ\u02d0", ipa.toString());
	}
	
	@Test
	public void testStrayInitialSuffixDiacritic() throws ParseException {
		final String txt = "ʷɔ\u02d0";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		Assert.assertEquals(2, ipa.length());
		Assert.assertEquals("Xɔ\u02d0", ipa.toString());
	}
	
	@Test
	public void testStrayFinalPrefixDiacritic() throws ParseException {
		final String txt = "ɔʷⁿ";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		Assert.assertEquals(2, ipa.length());
		Assert.assertEquals("ɔʷX", ipa.toString());
	}
	
	@Test
	public void testMissingCompoundPhone() throws ParseException {
		final String txt = "t\u035c t";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		Assert.assertEquals(3, ipa.length());
		Assert.assertEquals("t\u035cX t", ipa.toString());
	}
	
	@Ignore
	@Test
	public void testStrayInitialLigature() throws ParseException {
		final String txt = "\u035ct";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		Assert.assertEquals(1, ipa.length());
		Assert.assertEquals("t", ipa.toString());
	}
	
	@Ignore
	@Test
	public void testPauseLocationError() throws ParseException {
		final String txt = "hel(..)lo";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		Assert.assertEquals(6, ipa.length());
	}
}
