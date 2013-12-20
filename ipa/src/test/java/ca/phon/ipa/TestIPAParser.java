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
			final IPATranscript transcript = IPATranscript.parseTranscript(testString);
			if(transcript.size() < 1) {
				System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
			}
			Assert.assertEquals(1, transcript.size());
			
			final IPAElement ipaEle = transcript.get(0);
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
				final IPATranscript transcript = IPATranscript.parseTranscript(testString);
				if(transcript.size() < 1) {
					System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
				}
				Assert.assertEquals(1, transcript.size());
				
				final IPAElement ipaEle = transcript.get(0);
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
				final IPATranscript transcript = IPATranscript.parseTranscript(testString);
				if(transcript.size() < 1) {
					System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
				}
				Assert.assertEquals(1, transcript.size());
				
				final IPAElement ipaEle = transcript.get(0);
				Assert.assertEquals(Phone.class, ipaEle.getClass());
				
				final Phone p = (Phone)ipaEle;
				Assert.assertEquals(testString, p.getText());
				
				Assert.assertEquals(p.getSuffixDiacritic(), suffixChar);
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
				final IPATranscript transcript = IPATranscript.parseTranscript(testString);
				if(transcript.size() < 1) {
					System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
				}
				Assert.assertEquals(1, transcript.size());
				
				final IPAElement ipaEle = transcript.get(0);
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
				final IPATranscript transcript = IPATranscript.parseTranscript(testString);
				if(transcript.size() < 1) {
					System.err.println(testString + " " + Integer.toHexString((int)c.charValue()));
				}
				Assert.assertEquals(1, transcript.size());
				
				final IPAElement ipaEle = transcript.get(0);
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

}
