package ca.phon.ipa.parser;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

import ca.phon.ipa.parser.exceptions.IPAParserException;

public class UnicodeIPAParserErrorStrategy extends DefaultErrorStrategy {

	@Override
	public void recover(Parser recognizer, RecognitionException e) {
		throw new IPAParserException(e);
	}
	
	@Override
	public Token recoverInline(Parser recognizer) throws RecognitionException {
		throw new IPAParserException("XXX");
	}
	
}
