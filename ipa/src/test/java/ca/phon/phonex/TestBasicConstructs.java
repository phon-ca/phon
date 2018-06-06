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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import junit.framework.Assert;

/**
 * Test basic phonex constructs
 *
 */
@RunWith(JUnit4.class)
public class TestBasicConstructs {
	
	@Test
	public void testCompoundPhoneMatcher() throws ParseException {
		final String text = "st\u0361\u0283aad\u035c\u0292";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		// look for all compound phones
		final String phonex = "._.";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		int numFound = 0;
		while(matcher.find()) numFound++;
		Assert.assertEquals(2, numFound);
		
		// look for specific compound phones
	}
	
	@Test
	public void testRegexMatcher() throws ParseException {
		final String text =  "st\u0361\u0283aad\u035c\u0292";
		final String phonex = "'t.+'";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.find());
	}
	
	@Test
	public void testFeatureSetMatcher() throws ParseException {
		
	}

	@Test
	public void testUnicodeValueMatcher() throws ParseException {
		
	}

	@Test
	public void testAlignmentMarker() throws ParseException {
		final String text = "ba \u2194 ab";
		final String phonex = "(\\w+)\\b\u2194\\b(\\w+)";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.matches());
		Assert.assertEquals(ipa.subsection(0, 2).toList(), matcher.group(1));
		Assert.assertEquals(ipa.subsection(5, 7).toList(), matcher.group(2));
	}
	
	@Test
	public void testEmptyQuery() throws ParseException {
		final IPATranscript ipa = new IPATranscript();
		
		Assert.assertEquals(true, ipa.matches("^$"));
	}
	
	@Test
	public void testLookAheadBehind() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final String phonex = "(?<\\w)\\c(?>\\w)";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertTrue(matcher.find());
		Assert.assertEquals(ipa.elementAt(2), matcher.group().get(0));
	}
	
	@Test
	public void testLookBehindWithBoundary() throws ParseException {
		final String text = "ˈk:oʀ:oi:di:dt͡j:oi:n";
		// Beginning of input boundary marker must be inside look-behind group
		final String phonex = "(?<\\S\\c:L*)\\c:O(?>\\w:sctype(\"-O\"))";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertTrue(matcher.find());
		Assert.assertEquals(ipa.elementAt(5), matcher.group().get(0));
	}
	
	@Test
	public void testfOredGroup() throws ParseException {
		final String text1 = "ˈk:oʀ:oi:di:dt͡j:oi:n";
		final IPATranscript ipa1 = IPATranscript.parseIPATranscript(text1);
		
		final String phonex = "(k\\c|d\\c)\\v";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final PhonexMatcher matcher = pattern.matcher(ipa1);
		
		while(matcher.find()) {
			System.out.println(matcher.group());
		}
	}
	
}
