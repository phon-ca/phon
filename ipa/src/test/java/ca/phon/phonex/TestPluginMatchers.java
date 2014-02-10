package ca.phon.phonex;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;

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
	public void testAnyDiacriticMatcher() throws Exception {
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
	public void testScTypeMatcher() throws Exception {
		final String text = "s:Ltʰ:Ouː:Nd:Cbʷ:R";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		// TODO tests
	}
	
	
	
}
