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
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.features.FeatureMatrix;

@RunWith(JUnit4.class)
public class TestPredefinedPhoneClasses {

	@Test
	public void testConsonantPhoneClass() throws ParseException {
		final String phonex = "\\c";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Collection<Character> testChars = fm.getCharactersWithFeature("Consonant");
		
		// test all ipa characters
		for(Character c:testChars) {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(c + "");
			Assert.assertEquals(1, transcript.length());
			final PhonexMatcher matcher = pattern.matcher(transcript);
		
			Assert.assertEquals(true, matcher.matches());
		}
	}
	
	@Test
	public void testVowelPhoneClass() throws ParseException {
		final String phonex = "\\v";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Collection<Character> testChars = fm.getCharactersWithFeature("Vowel");
		
		// test all ipa characters
		for(Character v:testChars) {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(v + "");
			final PhonexMatcher matcher = pattern.matcher(transcript);
		
			Assert.assertEquals(true, matcher.matches());
		}
	}
	
	@Test
	public void testGlidePhoneClass() throws ParseException {
		final String phonex = "\\g";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Collection<Character> testChars = fm.getCharactersWithFeature("Glide");
		
		// test all ipa characters
		for(Character g:testChars) {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(g + "");
			final PhonexMatcher matcher = pattern.matcher(transcript);
		
			Assert.assertEquals(true, matcher.matches());
		}
	}
	
	@Test
	public void testWordClass() throws ParseException {
		final String phonex = "\\w";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Set<Character> testChars = new TreeSet<Character>();
		testChars.addAll(fm.getCharactersWithFeature("Consonant"));
		testChars.addAll(fm.getCharactersWithFeature("Vowel"));
		
		// test all ipa characters
		for(Character v:testChars) {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(v + "");
			final PhonexMatcher matcher = pattern.matcher(transcript);
		
			Assert.assertEquals(true, matcher.matches());
		}
	}
	
	@Test
	public void testPauseClass() throws ParseException {
		final String phonex = "\\p";
		final String txt = "one (.) two (..) three (...) four";
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher pm = pattern.matcher(ipa);
		
		final int[] locations = new int[]{ 4, 10, 18 };
		int idx = 0;
		while(pm.find()) {
			Assert.assertEquals(locations[idx++], pm.start());
		}
		Assert.assertEquals(3, idx);
	}
}
