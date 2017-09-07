/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.phonex;

import java.text.ParseException;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;

/**
 * Test phonex boundary detection.
 */
@RunWith(JUnit4.class)
public class TestBoundaries {

	@Test
	public void testBeginningOfInput() throws ParseException {
		final String text = "assassin";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		final String phonex = "^ass";
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		int numFound = 0;
		while(matcher.find()) numFound++;
		
		Assert.assertEquals(1, numFound);
	}
	
	@Test
	public void testEndOfInput() throws ParseException {
		final String text = "babababa";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		final String phonex = "ba$";
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		int numFound = 0;
		while(matcher.find()) numFound++;
		
		Assert.assertEquals(1, numFound);
	}
	
	@Test
	public void testWordBoundary() throws ParseException {
		final String text = "hello world";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		final String phonex = "\\b\\c.*\\v\\b";
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals(ipa.subsection(0, 6).toString(), new IPATranscript(matcher.group()).toString());
	}
	
	
	@Test
	public void testSyllableBoundaries() throws ParseException {
		final String text = "b:Oa:N\u02c8b:Oa:Nb:Oa:N";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
	
		final String phonex1 = "\\Sba";
		final PhonexPattern p1 = PhonexPattern.compile(phonex1);
		final PhonexMatcher m1 = p1.matcher(ipa);
		
		int numFound = 0;
		while(m1.find()) numFound++;
		Assert.assertEquals(3, numFound);
		
		final String phonex2 = "ba\\S";
		final PhonexPattern p2 = PhonexPattern.compile(phonex2);
		final PhonexMatcher m2 = p2.matcher(ipa);
		
		numFound = 0;
		while(m2.find()) numFound++;
		Assert.assertEquals(3, numFound);
	}
	
	@Test
	public void testIntraWordPause() throws ParseException {
		final String text = "te^st";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "\\^";
		Assert.assertEquals(2, ipa.indexOf(phonex));
	}
}
