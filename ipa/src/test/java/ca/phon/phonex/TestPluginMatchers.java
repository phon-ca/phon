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
	public void testPositionalDiacriticMatchers() throws Exception {
		
	}
	
	@Test
	public void testScTypeMatcher() throws Exception {
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
	
	
	
	@Test(expected=PhonexPluginException.class)
	public void testInvalidScType() throws Exception {
		final IPATranscript ipa = new IPATranscript();
		ipa.indexOf("\\c:Z");
	}
	
}
