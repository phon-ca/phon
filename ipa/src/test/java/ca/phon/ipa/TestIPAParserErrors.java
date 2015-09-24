/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.ipa;

import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import junit.framework.Assert;

@RunWith(JUnit4.class)
public class TestIPAParserErrors {
	
	@Test
	public void testInvalidTokenException() {
		final String txt = "e#j";
		
		try {
			final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
			Assert.fail(ipa.toString() + " passed");
		} catch (ParseException pe) {
//			Assert.assertEquals(InvalidTokenException.class, pe.getSuppressed()[0].getClass());
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
//			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
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
//			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
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
//			Assert.assertEquals(HangingLigatureException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(2, pe.getErrorOffset());
		}
		
		txt = "\u035c";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
//			Assert.assertEquals(HangingLigatureException.class, pe.getSuppressed()[0].getClass());
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
//			Assert.assertEquals(HangingLigatureException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(0, pe.getErrorOffset());
		}
		
		txt = "ab \u035ct";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
//			Assert.assertEquals(HangingLigatureException.class, pe.getSuppressed()[0].getClass());
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
//			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(0, pe.getErrorOffset());
		}
		
		txt = "ab \u02d0t";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
//			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
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
//			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(0, pe.getErrorOffset());
		}
		
		txt = "ab \u00b2t";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
//			Assert.assertEquals(StrayDiacriticException.class, pe.getSuppressed()[0].getClass());
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
			Assert.assertEquals(3, pe.getErrorOffset());
		}
	}
	
	@Test
	public void testInvalidCompound() throws ParseException {
		String txt = "e\u0361\u007c";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
//			Assert.assertEquals(InvalidTokenException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(2, pe.getErrorOffset());
		}
		
		txt = "ʰ͡|ˑeːː͡ɪ̃n";
		try {
			IPATranscript.parseIPATranscript(txt);
			Assert.fail(txt + " passed");
		} catch (ParseException pe) {
//			Assert.assertEquals(InvalidTokenException.class, pe.getSuppressed()[0].getClass());
			Assert.assertEquals(2, pe.getErrorOffset());
		}
	}
	
	@Test
	public void testIntrWordNumber() throws ParseException {
		final String txt = "he6lo";
		try {
			IPATranscript.parseIPATranscript(txt);
		} catch (ParseException pe) {
			Assert.assertEquals(2, pe.getErrorOffset());
		}
	}
	
}
