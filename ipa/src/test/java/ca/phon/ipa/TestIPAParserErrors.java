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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.parser.exceptions.HangingLigatureException;
import ca.phon.ipa.parser.exceptions.IPAParserException;
import ca.phon.ipa.parser.exceptions.InvalidTokenException;
import ca.phon.ipa.parser.exceptions.StrayDiacriticException;

@RunWith(JUnit4.class)
public class TestIPAParserErrors {
	
	/**
	 * Returns the exception thrown by the given invalid IPA
	 * transcript.  If the parser passes, null is returned.
	 * 
	 * @param txt
	 * @return
	 */
	private ParseException testParser(String txt) {
		ParseException retVal = null;
		
		try {
			IPATranscript.parseIPATranscript(txt);
		} catch (ParseException pe) {
			retVal = pe;
		}
		
		return retVal;
	}
	
	private void testError(String txt, int location, Class<? extends IPAParserException> errorType) {
		final ParseException pe = testParser(txt);
		Assert.assertNotNull(pe);
		Assert.assertEquals(location, pe.getErrorOffset());
		Assert.assertEquals(errorType, pe.getSuppressed()[0].getClass());
	}
	
	@Test
	public void testInvalidTokenException() {
		final String txt = "e#j";
		testError(txt, 1, InvalidTokenException.class);
	}

	@Test
	public void testStrayInitialSuffixDiacritic() throws ParseException {
		final String txt = "ʷɔ\u02d0";
		testError(txt, 1, StrayDiacriticException.class);
	}
	
	@Test
	public void testStrayFinalPrefixDiacritic() {
		final String txt = "ɔʷⁿ";
		testError(txt, 3, StrayDiacriticException.class);
	}
	
	@Test
	public void testHangingLigature() throws ParseException {
		String txt = "t\u035c";
		testError(txt, 2, HangingLigatureException.class);
		
		txt = "\u035c";
		testError(txt, 0, HangingLigatureException.class);
	}
	
	@Test
	public void testStrayInitialLigature() {
		String txt = "\u035ct";
		testError(txt, 0, HangingLigatureException.class);
		
		txt = "ab \u035ct";
		testError(txt, 3, HangingLigatureException.class);
	}
	
	@Test
	public void testStrayLengthDiacritic() {
		String txt = "\u02d0t";
		testError(txt, 0, StrayDiacriticException.class);
		
		txt = "ab \u02d0t";
		testError(txt, 3, StrayDiacriticException.class);
	}
	
	@Test
	public void testStrayToneDiacritic() {
		String txt = "\u00b2t";
		testError(txt, 0, StrayDiacriticException.class);
		
		txt = "ab \u00b2t";
		testError(txt, 3, StrayDiacriticException.class);
	}
	
	@Test
	public void testPauseLocationError() {
		final String txt = "hel(..)lo";
		testError(txt, 3, IPAParserException.class);
	}
	
	@Test
	public void testInvalidCompound() {
		String txt = "e\u0361\u007c";
		testError(txt, 2, InvalidTokenException.class);
		
		txt = "ʰ͡|ˑeːː͡ɪ̃n";
		testError(txt, 2, InvalidTokenException.class);
	}
	
	@Test
	public void testIntrWordNumber() {
		final String txt = "he6lo";
		testError(txt, 2, IPAParserException.class);
	}
	
	@Test
	public void testExtraIntraWordSpace() {
		final String txt = "helo  wrld";
		testError(txt, 6, IPAParserException.class);
	}
	
	@Test
	public void testDoubleSyllableBoundary()  {
		String txt = "hel..o";
		testError(txt, 5, StrayDiacriticException.class);
		
		txt = "hel.\u02c8o";
		testError(txt, 5, StrayDiacriticException.class);
		
		txt = "hel\u02c8\u02c8o";
		testError(txt, 5, StrayDiacriticException.class);
	}
	
	@Test
	public void testHangingSyllableBoundary() {
		String txt = "helo\u02c8";
		testError(txt, 5, StrayDiacriticException.class);
		
		txt = "helo\u02cc";
		testError(txt, 5, StrayDiacriticException.class);
		
		txt = "helo.";
		testError(txt, 5, StrayDiacriticException.class);
		
	}
	
}
