package ca.phon.ipa.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class UnicodeIPAParserErrorListener extends BaseErrorListener {

	private List<ParseException> parseExceptions = new ArrayList<>();

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		ParseException pe = new ParseException(msg, charPositionInLine);
		parseExceptions.add(pe);
	}

	public List<ParseException> getParseExceptions() {
		return this.parseExceptions;
	}

}
