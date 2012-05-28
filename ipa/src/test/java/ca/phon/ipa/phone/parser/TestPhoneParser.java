package ca.phon.ipa.phone.parser;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.phon.ipa.IPATokenType;
import ca.phon.ipa.IPATokens;

public class TestPhoneParser extends TestCase {
	
	/* static logger for class */
	private static final Logger LOGGER = Logger.getLogger(TestPhoneParser.class
			.getName());
	
	/**
	 * Test phone parser with various strings
	 */
	public void testBasePhones() {
		IPATokens tokenMapper = IPATokens.getSharedInstance();
		// gather all CONSONANTS, VOWELS, GLIDES and COVER_SYMBOLs
		Set<Character> consonants = tokenMapper.getCharactersForType(IPATokenType.CONSONANT);
		Set<Character> vowels = tokenMapper.getCharactersForType(IPATokenType.VOWEL);
		Set<Character> glides = tokenMapper.getCharactersForType(IPATokenType.GLIDE);
		Set<Character> coverSymbols = tokenMapper.getCharactersForType(IPATokenType.COVER_SYMBOL);
		
		Set<Character> allBasePhones = new TreeSet<Character>();
		allBasePhones.addAll(consonants);
		allBasePhones.addAll(vowels);
		allBasePhones.addAll(glides);
		allBasePhones.addAll(coverSymbols);
		
		for(Character glyph:allBasePhones) {
			String phoneString = glyph + "";
			performTest(phoneString);
		}
	}
	
	/**
	 * Test pauses in transcriptions
	 */
	public void testPauses() {
		String testStrings[] = {
				"short pa(.)use",
				"medium pa(..)use",
				"long pa(...)use"
		};
		
		for(String testString:testStrings)
			performTest(testString);
	}
	
	/**
	 * Test phone parser with the given string
	 * 
	 * @param testString
	 * 
	 */
	private void performTest(String testString) {
		TestHandler handler = new TestHandler();
		
		PhoneLexer lexer = new PhoneLexer(testString);
		lexer.addErrorHandler(handler);
		TokenStream tokenStream = new CommonTokenStream(lexer);
		PhoneParser parser = new PhoneParser(tokenStream);
		parser.addErrorHandler(handler);
		
		try {
			parser.transcription();
		} catch (RecognitionException e) {
			e.printStackTrace();
			throw new AssertionError(e);
		}
		
		assertEquals(0, handler.numErrors);
	}
	
	private class TestHandler implements PhoneParserErrorHandler {
		
		int numErrors = 0;
		
		@Override
		public void handleError(PhoneParserException ex) {
			numErrors++;
		}
		
	}
}
