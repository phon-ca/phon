package ca.phon.ipa.phone.parser;

import java.util.Set;
import java.util.logging.Logger;

import org.antlr.runtime.Token;

import ca.phon.ipa.IPATokens;
import junit.framework.TestCase;

/**
 * Test the custom ANTLR Phone lexer
 *
 */
public class TestPhoneLexer extends TestCase {
	
	/* static logger for class */
	private static final Logger LOGGER = Logger.getLogger(TestPhoneLexer.class
			.getName());
	
	/**
	 * Test all possible tokens in a long string
	 */
	public void testTokenLanguage() {
		IPATokens tokenMap = IPATokens.getSharedInstance();
		String testString = "";
		
		Set<Character> ipaCharSet = tokenMap.getCharacterSet();
		for(Character ipaChar:ipaCharSet) {
			testString += ipaChar + "";
		}
		
		TestHandler handler = new TestHandler();
		PhoneLexer lexer = new PhoneLexer(testString);
		lexer.addErrorHandler(handler);
		int numLexed = 0;
		while(lexer.nextToken() != Token.EOF_TOKEN) {
			numLexed++;
		}
		
		assertEquals(0, handler.numErrors);
		assertEquals(ipaCharSet.size(), numLexed);
	}

	private class TestHandler implements PhoneParserErrorHandler {
		
		int numErrors = 0;
		
		@Override
		public void handleError(PhoneParserException ex) {
			numErrors++;
			LOGGER.severe(ex.getMessage());
		}
		
	}
}
