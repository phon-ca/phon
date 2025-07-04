package ca.phon.ipa.parser;

import ca.phon.ipa.parser.exceptions.*;
import org.antlr.v4.runtime.*;

import java.util.*;

public class UnicodeIPAParserErrorListener extends BaseErrorListener {

	private List<IPAParserException> parseExceptions = new ArrayList<>();

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		InvalidTokenException ex = new InvalidTokenException(msg);
		ex.setPositionInLine(charPositionInLine);
		parseExceptions.add(ex);
	}

	public List<IPAParserException> getParseExceptions() {
		return this.parseExceptions;
	}

}
