package ca.phon.ipa;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Test methods for the ipa parser.
 *
 */
@RunWith(JUnit4.class)
public class TestIPAParser {
	
	/**
	 * Test each individual consonant and vowel.
	 * 
	 */
	@Test
	public void testIndividualPhones() throws ParseException {
		final List<Character> testChars = new ArrayList<Character>();
		final IPATokens tokens = IPATokens.getSharedInstance();
		testChars.addAll(tokens.getCharactersForType(IPATokenType.CONSONANT));
		testChars.addAll(tokens.getCharactersForType(IPATokenType.VOWEL));
		
		for(Character c:testChars) {
			final String testString = "" + c;
			final IPATranscript transcript = IPATranscript.parseIPATranscript(testString);
			if(transcript.length() < 1) {
				System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
			}
			Assert.assertEquals(1, transcript.length());
			
			final IPAElement ipaEle = transcript.elementAt(0);
			Assert.assertEquals(Phone.class, ipaEle.getClass());
			
			final Phone p = (Phone)ipaEle;
			Assert.assertEquals(testString, p.getText());
		}
	}
	
	@Test
	public void testPrefixDiacritics() throws ParseException {
		final IPATokens tokens = IPATokens.getSharedInstance();
		final List<Character> testChars = new ArrayList<Character>();
		testChars.addAll(tokens.getCharactersForType(IPATokenType.CONSONANT));
		testChars.addAll(tokens.getCharactersForType(IPATokenType.VOWEL));
		final Set<Character> prefixChars = tokens.getCharactersForType(IPATokenType.PREFIX_DIACRITIC);
		
		for(Character c:testChars) {
			for(Character prefixChar:prefixChars) {
				final String testString = prefixChar + "" + c;
				final IPATranscript transcript = IPATranscript.parseIPATranscript(testString);
				if(transcript.length() < 1) {
					System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
				}
				Assert.assertEquals(1, transcript.length());
				
				final IPAElement ipaEle = transcript.elementAt(0);
				Assert.assertEquals(Phone.class, ipaEle.getClass());
				
				final Phone p = (Phone)ipaEle;
				Assert.assertEquals(testString, p.getText());
				
				Assert.assertEquals(p.getPrefixDiacritic(), prefixChar);
				Assert.assertEquals(p.getBasePhone(), c);
			}
		}
	}
	
	@Test
	public void testSuffixDiacritics() throws ParseException {
		final IPATokens tokens = IPATokens.getSharedInstance();
		final List<Character> testChars = new ArrayList<Character>();
		testChars.addAll(tokens.getCharactersForType(IPATokenType.CONSONANT));
		testChars.addAll(tokens.getCharactersForType(IPATokenType.VOWEL));
		final Set<Character> suffixChars = tokens.getCharactersForType(IPATokenType.SUFFIX_DIACRITIC);
		
		for(Character c:testChars) {
			for(Character suffixChar:suffixChars) {
				final String testString = c + "" + suffixChar;
				final IPATranscript transcript = IPATranscript.parseIPATranscript(testString);
				if(transcript.length() < 1) {
					System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
				}
				Assert.assertEquals(1, transcript.length());
				
				final IPAElement ipaEle = transcript.elementAt(0);
				Assert.assertEquals(Phone.class, ipaEle.getClass());
				
				final Phone p = (Phone)ipaEle;
				Assert.assertEquals(testString, p.getText());
				
				Assert.assertEquals(suffixChar, p.getSuffixDiacritic());
				Assert.assertEquals(p.getBasePhone(), c);
			}
		}
	}
	
	@Test
	public void testReversedDiacritics() throws ParseException {
		final IPATokens tokens = IPATokens.getSharedInstance();
		final List<Character> testChars = new ArrayList<Character>();
		final char roleReverse = '\u0335';
		testChars.addAll(tokens.getCharactersForType(IPATokenType.CONSONANT));
		testChars.addAll(tokens.getCharactersForType(IPATokenType.VOWEL));
		final Set<Character> prefixChars = tokens.getCharactersForType(IPATokenType.PREFIX_DIACRITIC);
		final Set<Character> suffixChars = tokens.getCharactersForType(IPATokenType.SUFFIX_DIACRITIC);
		
		for(Character c:testChars) {
			for(Character prefixChar:prefixChars) {
				final String testString = c + "" + prefixChar + "" + roleReverse;
				final IPATranscript transcript = IPATranscript.parseIPATranscript(testString);
				if(transcript.length() < 1) {
					System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
				}
				Assert.assertEquals(1, transcript.length());
				
				final IPAElement ipaEle = transcript.elementAt(0);
				Assert.assertEquals(Phone.class, ipaEle.getClass());
				
				final Phone p = (Phone)ipaEle;
				Assert.assertEquals(testString, p.getText());
				
				Assert.assertEquals(p.getSuffixDiacritic(), prefixChar);
				Assert.assertEquals(p.getBasePhone(), c);
			}
		}
		
		for(Character c:testChars) {
			for(Character suffixChar:suffixChars) {
				final String testString = suffixChar + "" + roleReverse + "" + c;
				final IPATranscript transcript = IPATranscript.parseIPATranscript(testString);
				if(transcript.length() < 1) {
					System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
				}
				Assert.assertEquals(1, transcript.length());
				
				final IPAElement ipaEle = transcript.elementAt(0);
				Assert.assertEquals(Phone.class, ipaEle.getClass());
				
				final Phone p = (Phone)ipaEle;
				Assert.assertEquals(testString, p.getText());
				
				Assert.assertEquals(p.getPrefixDiacritic(), suffixChar);
				Assert.assertEquals(p.getBasePhone(), c);
			}
		}
	}
	
	@Test
	public void testCombinedDiacritics() {
		
	}
	
	@Test
	public void testCompoundMarker() throws Exception {
		final String testString = "yes+sir";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(testString);
		
		Assert.assertEquals(7, ipa.length());
		Assert.assertEquals(CompoundWordMarker.class, ipa.elementAt(3).getClass());
	}
	
	@Test
	public void testLinker() throws Exception {
		final String testString = "yes\u2040sir";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(testString);
		
		Assert.assertEquals(7, ipa.length());
		Assert.assertEquals(Linker.class, ipa.elementAt(3).getClass());
	}
	
	@Test
	public void testContraction() throws Exception {
		final String testString = "yes\u203fsir";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(testString);
		
		Assert.assertEquals(7, ipa.length());
		Assert.assertEquals(Contraction.class, ipa.elementAt(3).getClass());
	}
	
	@Test
	public void testEmbeddedSyllabification() throws Exception {
		final String testString = "s:Eh:Le:Ol:Nl:Co:R";
		final IPATranscript transcript = IPATranscript.parseIPATranscript(testString);
		
		Assert.assertEquals(6, transcript.length());
		Assert.assertEquals(SyllableConstituentType.OEHS, transcript.elementAt(0).getScType());
		Assert.assertEquals(SyllableConstituentType.LEFTAPPENDIX, transcript.elementAt(1).getScType());
		Assert.assertEquals(SyllableConstituentType.ONSET, transcript.elementAt(2).getScType());
		Assert.assertEquals(SyllableConstituentType.NUCLEUS, transcript.elementAt(3).getScType());
		Assert.assertEquals(SyllableConstituentType.CODA, transcript.elementAt(4).getScType());
		Assert.assertEquals(SyllableConstituentType.RIGHTAPPENDIX, transcript.elementAt(5).getScType());
	}

}
