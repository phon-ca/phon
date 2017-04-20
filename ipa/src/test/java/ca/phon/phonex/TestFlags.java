package ca.phon.phonex;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import junit.framework.Assert;

@RunWith(JUnit4.class)
public class TestFlags {

	@Test
	public void testOverlappingMatches() throws Exception {
		final String phonex = "\\c\\v\\c\\v/o";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "badafagabadafa";

		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseIPATranscript(ipa));
		int numMatches = 0;
		while(matcher.find()) {
			++numMatches;
		}
		Assert.assertEquals(6, numMatches);
	}

	/*
	@Test
	public void testStripPunctuation() throws Exception {
		final String phonex = "\\vk:O/p";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "ˌ:Sh:Oa:Dɪ:Dp:Oə:Nˈ:Sk:Oɑ:Nn:Cd:Oɹ:Oiː:Næ:Nk:C";

		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseIPATranscript(ipa));

		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals((new IPATranscript(matcher.group())).toString(), "ək");
	}
	*/

	/*
	@Test
	public void testStripDiacritics() throws Exception {
		final String phonex = "\\c\\c/d";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "s:Ltʰ:Ouː:Nd:Cbʷ:Rd:E";

		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseIPATranscript(ipa));

		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals((new IPATranscript(matcher.group())).toString(), "st");
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals((new IPATranscript(matcher.group())).toString(), "db");
	}
	*/

	@Test(expected=PhonexPatternException.class)
	public void testInvalidFlag() throws Exception {
		final String phonex = "\\c\\v/z";
		PhonexPattern.compile(phonex);
	}

}
