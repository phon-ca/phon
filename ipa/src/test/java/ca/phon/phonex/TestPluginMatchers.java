/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.phonex;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;

/**
 * Test built-in plug-in matchers
 *
 */
@RunWith(JUnit4.class)
public class TestPluginMatchers extends PhonexTest {

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

		Assert.assertEquals(2, ipa.indexOf("\\v&{long}"));
	}

	@Test
	public void testLongDiacritics() throws ParseException {
		final String text = "u\u02d0";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);

		// using 'any diacritic' matcher
		Assert.assertEquals(0, ipa.indexOf("u&{long}"));
		// using shorthand
		Assert.assertEquals(0, ipa.indexOf("u\u02d0"));
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
	public void testCombinedScType() throws ParseException {
		final String text = "s:Ltʰ:Ouː:Nd:Cbʷ:Rd:E";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(\\c:L:O+)";
		final IPATranscript[][] answers = new IPATranscript[][] { 
			{ ipa.subsection(0, 2) }
		};
		testGroups(ipa, phonex, answers);
	}

	@Test
	public void testStressMatcher() throws ParseException {
		final String txt = "ˌh:Oa:Dɪ:Dp:Oə:Nˈk:Oɑ:Nn:Cd:Oɹ:Oiː:Næ:Nk:C";
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
	public void testPrefixMatcher() throws ParseException {
		final String txt = "stʰuːⁿdbʷ";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		Assert.assertEquals(3, ipa.indexOf(".:prefix(\"{nasal}\")"));
	}
	
	@Test
	public void testSuffixMatcher() throws ParseException {
		final String txt = "stʰuːⁿdbʷ";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		
		Assert.assertEquals(1, ipa.indexOf(".:suffix(\"{aspirated}\")"));
		Assert.assertEquals(4, ipa.indexOf(".:suffix(\"{labial}\")"));
	}

    @Test
    public void testToneNumberMatcher() throws ParseException {
        // Any σ²¹⁴ followed by another σ²¹⁴
        final String phonex = "(σ:tn(\"214\"))(?>σ:tn(\"214\"))";
        final PhonexPattern pattern = PhonexPattern.compile(phonex);

        final String ipa = "mɔ²¹⁴ɕi²¹⁴ kʰɔ²¹⁴ɕi²¹⁴ mɔ³³ɕi³³";

        IPATranscript transcript = IPATranscript.parseIPATranscript(ipa);
        final Syllabifier syllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage("cmn");
        transcript = syllabifier.syllabify(transcript);

        final PhonexMatcher matcher = pattern.matcher(transcript);
        Assert.assertTrue(matcher.find());
        final IPATranscript grpIpa = new IPATranscript(matcher.group(1));
        Assert.assertEquals("mɔ²¹⁴", grpIpa.toString());
        Assert.assertTrue(matcher.find());
        final IPATranscript grpIpa2 = new IPATranscript(matcher.group(1));
        Assert.assertEquals("kʰɔ²¹⁴", grpIpa2.toString());
        Assert.assertFalse(matcher.find());
    }

    @Test
    public void testToneNumberReplace() throws ParseException {
        // Any σ²¹⁴ followed by another σ²¹⁴
        final String phonex = "((σ/S..R/:tn(\"214\"))\\t)(?>σ:tn(\"214\"))";
        final PhonexPattern pattern = PhonexPattern.compile(phonex);

        final String ipa = "mɔ²¹⁴ɕi²¹⁴ kʰɔ²¹⁴ɕi²¹⁴ mɔ³³ɕi³³";

        IPATranscript transcript = IPATranscript.parseIPATranscript(ipa);
        final Syllabifier syllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage("cmn");
        transcript = syllabifier.syllabify(transcript);

        final PhonexMatcher matcher = pattern.matcher(transcript);
        Assert.assertTrue(matcher.find());

        final IPATranscriptBuilder builder = new IPATranscriptBuilder();
        // replace our matched groups tone with tone 51
        final IPATranscript replacement = IPATranscript.parseIPATranscript("\\2⁵¹");
        matcher.appendReplacement(builder, replacement);
        matcher.appendTail(builder);
        final IPATranscript replaced = builder.toIPATranscript();
        Assert.assertEquals("mɔ⁵¹ɕi²¹⁴ kʰɔ²¹⁴ɕi²¹⁴ mɔ³³ɕi³³", replaced.toString());

        Assert.assertTrue(matcher.find());
        final IPATranscript grpIpa2 = new IPATranscript(matcher.group(1));
        Assert.assertEquals("kʰɔ²¹⁴", grpIpa2.toString());
        Assert.assertFalse(matcher.find());
    }

}
