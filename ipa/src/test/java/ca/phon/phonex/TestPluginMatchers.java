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
 * Test built-in plug-in matchers
 *
 */
@RunWith(JUnit4.class)
public class TestPluginMatchers {

	@Test(expected=NoSuchPluginException.class)
	public void testNoSuchPlugin() throws Exception {
		// throws an exception since plug-in does not exist
		PhonexPattern.compile("\\c:nosuchplugin()");
	}

	@Test
	public void testAnyDiacriticMatcher() throws ParseException {
		final String text = "stʰuːdbʷ";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
	
		// test different versions of the same expression
		Assert.assertEquals(1, ipa.indexOf("\\c&{aspirated}"));
		Assert.assertEquals(1, ipa.indexOf("\\c:diacritic(\"{aspirated}\")"));
		
		Assert.assertEquals(1, ipa.indexOf("\\c&ʰ"));
		Assert.assertEquals(1, ipa.indexOf("\\c:diacritic(\"ʰ\")"));
	
		Assert.assertEquals(3, ipa.indexOf("\\c&{}", 2));
		Assert.assertEquals(3, ipa.indexOf("\\c:diacritic(\"{}\")", 2));
		
		Assert.assertEquals(4, ipa.indexOf("\\c&{labial}"));
		Assert.assertEquals(4, ipa.indexOf("\\c:diacritic(\"{labial}\")"));
		
		Assert.assertEquals(1, ipa.indexOf("\\c&[ʰʷ]"));
		Assert.assertEquals(1, ipa.indexOf("\\c:diacritic(\"[ʰʷ]\")"));
		
		Assert.assertEquals(4, ipa.indexOf("\\c&[ʰʷ]", 2));
		Assert.assertEquals(4, ipa.indexOf("\\c:diacritic(\"[ʰʷ]\")", 2));
	}
	
	@Test
	public void testPositionalDiacriticMatchers() throws Exception {
		
	}
	
	@Test
	public void testScTypeMatcher() throws ParseException {
		final String text = "s:Ltʰ:Ouː:Nd:Cbʷ:Rd:E";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		// left appendix
		Assert.assertEquals(0, ipa.indexOf(".:L"));
		Assert.assertEquals(0, ipa.indexOf(".:l"));
		Assert.assertEquals(0, ipa.indexOf(".:sctype(\"LA\")"));
		Assert.assertEquals(0, ipa.indexOf(".:sctype(\"LeftAppendix\")"));
		Assert.assertEquals(1, ipa.indexOf(".:-L"));
		Assert.assertEquals(1, ipa.indexOf(".:sctype(\"-LeftAppendix\")"));
		
		// onset
		Assert.assertEquals(1, ipa.indexOf(".:O"));
		Assert.assertEquals(1, ipa.indexOf(".:o"));
		Assert.assertEquals(1, ipa.indexOf(".:sctype(\"O\")"));
		Assert.assertEquals(1, ipa.indexOf(".:sctype(\"Onset\")"));
		Assert.assertEquals(2, ipa.indexOf(".:-O", 1));
		Assert.assertEquals(2, ipa.indexOf(".:sctype(\"-Onset\")", 1));
		
		// nucleus
		Assert.assertEquals(2, ipa.indexOf(".:N"));
		Assert.assertEquals(2, ipa.indexOf(".:n"));
		Assert.assertEquals(2, ipa.indexOf(".:sctype(\"N\")"));
		Assert.assertEquals(2, ipa.indexOf(".:sctype(\"Nucleus\")"));
		Assert.assertEquals(3, ipa.indexOf(".:-N", 2));
		Assert.assertEquals(3, ipa.indexOf(".:sctype(\"-Nucleus\")", 2));
		
		// coda
		Assert.assertEquals(3, ipa.indexOf(".:C"));
		Assert.assertEquals(3, ipa.indexOf(".:c"));
		Assert.assertEquals(3, ipa.indexOf(".:sctype(\"C\")"));
		Assert.assertEquals(3, ipa.indexOf(".:sctype(\"Coda\")"));
		Assert.assertEquals(4, ipa.indexOf(".:-C", 3));
		Assert.assertEquals(4, ipa.indexOf(".:sctype(\"-Coda\")", 3));
		
		// right appendix
		Assert.assertEquals(4, ipa.indexOf(".:R"));
		Assert.assertEquals(4, ipa.indexOf(".:r"));
		Assert.assertEquals(4, ipa.indexOf(".:sctype(\"R\")"));
		Assert.assertEquals(4, ipa.indexOf(".:sctype(\"RightAppendix\")"));
		Assert.assertEquals(5, ipa.indexOf(".:-R", 4));
		Assert.assertEquals(5, ipa.indexOf(".:sctype(\"-RightAppendix\")", 4));
		
		// oehs
		Assert.assertEquals(5, ipa.indexOf(".:E"));
		Assert.assertEquals(5, ipa.indexOf(".:e"));
		Assert.assertEquals(5, ipa.indexOf(".:sctype(\"E\")"));
		Assert.assertEquals(5, ipa.indexOf(".:sctype(\"OEHS\")"));
	}
	
	@Test
	public void testStressMatcher() throws ParseException {
		final String txt = "ˌ:Sh:Oa:Dɪ:Dp:Oə:Nˈ:Sk:Oɑ:Nn:Cd:Oɹ:Oiː:Næ:Nk:C";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		final IPATranscript[] unstressed = new IPATranscript[] {
			ipa.subsection(4, 6),
			ipa.subsection(10, ipa.length())
		};
		final IPATranscript primaryStressed = ipa.subsection(6, 10);
		final IPATranscript secondaryStressed = ipa.subsection(0, 4);
		final IPATranscript[] stressed = new IPATranscript[] { secondaryStressed, primaryStressed };
		
		// test no stress
		PhonexPattern pattern = PhonexPattern.compile(".!U+");
		PhonexMatcher matcher = pattern.matcher(ipa);
		int numUnstressedFound = 0;
		while(matcher.find()) {
			Assert.assertEquals(true, numUnstressedFound < unstressed.length);
			Assert.assertEquals(unstressed[numUnstressedFound++], new IPATranscript(matcher.group()));
		}
		Assert.assertEquals(unstressed.length, numUnstressedFound);
		
		// test primary stress
		pattern = PhonexPattern.compile(".!1+");
		matcher = pattern.matcher(ipa);
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals(primaryStressed, new IPATranscript(matcher.group()));
		
		// test secondary stress
		pattern = PhonexPattern.compile(".:stress(\"2\")+");
		matcher = pattern.matcher(ipa);
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals(secondaryStressed, new IPATranscript(matcher.group()));
		
		// test multiple stress
		pattern = PhonexPattern.compile(".:stress(\"1|2\")+");
		matcher = pattern.matcher(ipa);
		int numStressedFound = 0;
		while(matcher.find()) {
			Assert.assertEquals(true, numStressedFound < unstressed.length);
			Assert.assertEquals(stressed[numStressedFound++], new IPATranscript(matcher.group()));
		}
		Assert.assertEquals(stressed.length, numStressedFound);
		
		// test any stress
		pattern = PhonexPattern.compile(".!S+");
		matcher = pattern.matcher(ipa);
		numStressedFound = 0;
		while(matcher.find()) {
			Assert.assertEquals(true, numStressedFound < unstressed.length);
			Assert.assertEquals(stressed[numStressedFound++], new IPATranscript(matcher.group()));
		}
		Assert.assertEquals(stressed.length, numStressedFound);
	}

	@Test
	public void testToneMatcher() throws ParseException {
		String txt = "t:Oo:Nm¹:Cn:Oo:Nm:Ct:Oɛ:Np⁵:C";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);

		final IPATranscript tone1 = ipa.subsection(0, 3);
		final IPATranscript tone2 = ipa.subsection(6, ipa.length());
		final IPATranscript[] allTones = {
				tone1, tone2
		};
		
		// tone1
		PhonexPattern pattern = PhonexPattern.compile(".:tone(\"1\")+");
		PhonexMatcher matcher = pattern.matcher(ipa);
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals(tone1, new IPATranscript(matcher.group()));
		
		// tone5
		pattern = PhonexPattern.compile(".:tone(\"5\")+");
		matcher = pattern.matcher(ipa);
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals(tone2, new IPATranscript(matcher.group()));
		
		// any tone
		pattern = PhonexPattern.compile(".:tone(\"*\")+");
		matcher = pattern.matcher(ipa);
		
		int numFound = 0;
		while(matcher.find()) {
			Assert.assertEquals(true, numFound < allTones.length);
			Assert.assertEquals(allTones[numFound++], new IPATranscript(matcher.group()));
		}
		Assert.assertEquals(allTones.length, numFound);
	}
	
}
