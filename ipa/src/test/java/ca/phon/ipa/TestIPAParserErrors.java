package ca.phon.ipa;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.parser.exceptions.HangingLigatureException;
import ca.phon.ipa.parser.exceptions.InvalidTokenException;
import ca.phon.ipa.parser.exceptions.StrayDiacriticException;

@RunWith(JUnit4.class)
public class TestIPAParserErrors {
	
	@Test
	public void testInvalidTokenException() {
		final String txt = "e#j";
		
		try {
			final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
			Assert.fail(ipa.toString() + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(InvalidTokenException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(1, pe.getErrorOffset());
		}
	}

	@Test
	public void testStrayInitialSuffixDiacritic() {
		final String txt = "ʷɔ\u02d0";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(1, pe.getErrorOffset());
		}
	}
	
	@Test
	public void testStrayFinalPrefixDiacritic() {
		final String txt = "ɔʷⁿ";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(3, pe.getErrorOffset());
		}
	}
	
	@Test
	public void testHangingLigature() {
		String txt = "t\u035c";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(HangingLigatureException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(2, pe.getErrorOffset());
		}
		
		txt = "\u035c";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(HangingLigatureException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(0, pe.getErrorOffset());
		}
		
		txt = "";
		
	}
	
	
	@Test
	public void testStrayInitialLigature() {
		String txt = "\u035ct";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(HangingLigatureException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(0, pe.getErrorOffset());
		}
		
		txt = "ab \u035ct";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(HangingLigatureException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(3, pe.getErrorOffset());
		}
	}
	
	@Test
	public void testStrayLengthDiacritic() {
		String txt = "\u02d0t";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(0, pe.getErrorOffset());
		}
		
		txt = "ab \u02d0t";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(3, pe.getErrorOffset());
		}
	}
	
	@Test
	public void testStrayToneDiacritic() {
		String txt = "\u00b2t";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(0, pe.getErrorOffset());
		}
		
		txt = "ab \u00b2t";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(3, pe.getErrorOffset());
		}
	}
	
	@Test
	public void testPauseLocationError() throws ParseException {
		final String txt = "hel(..)lo";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(6, pe.getErrorOffset());
		}
	}
	
	@Test
	public void testInvalidCompound() throws ParseException {
		String txt = "e\u0361\u007c";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(InvalidTokenException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(2, pe.getErrorOffset());
		}
		
		txt = "ʰ͡|ˑeːː͡ɪ̃n";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
			Assert.assertEquals(InvalidTokenException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(2, pe.getErrorOffset());
		}
	}
	
}
