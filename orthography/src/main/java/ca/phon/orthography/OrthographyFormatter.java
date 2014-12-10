package ca.phon.orthography;

import java.text.ParseException;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

/**
 * Orthography formatter
 *
 */
@FormatterType(Orthography.class)
public class OrthographyFormatter implements Formatter<Orthography> {

	@Override
	public String format(Orthography obj) {
		return obj.toString();
	}

	@Override
	public Orthography parse(String text) throws ParseException {
		Orthography retVal = new Orthography();
		final OrthoTokenSource tokenSource = new OrthoTokenSource(text);
		final TokenStream tokenStream = new CommonTokenStream(tokenSource);
		final OrthographyParser parser = new OrthographyParser(tokenStream);
		try {
			retVal = parser.orthography().ortho;
		} catch (RecognitionException e) {
			throw new ParseException(text, e.charPositionInLine);
		}
		return retVal;
	}

}
