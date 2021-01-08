package ca.phon.ipa.parser;

import ca.phon.ipa.WordBoundary;
import ca.phon.ipa.parser.exceptions.HangingLigatureException;
import ca.phon.ipa.parser.exceptions.IPAParserException;
import ca.phon.ipa.parser.exceptions.InvalidTokenException;
import org.antlr.v4.runtime.*;

public class UnicodeIPAParserErrorStrategy extends DefaultErrorStrategy {

	private UnicodeIPAParserListener listener;

	public UnicodeIPAParserErrorStrategy(UnicodeIPAParserListener listener) {
		this.listener = listener;
	}

	@Override
	public void recover(Parser recognizer, RecognitionException e) {
		IPAParserException ex = null;
		if(e instanceof InputMismatchException) {
			if (e.getOffendingToken().getType() == CommonToken.EOF) {
				// check previous token
				if (e.getOffendingToken().getTokenIndex() > 0) {
					Token token = recognizer.getInputStream().get(e.getOffendingToken().getTokenIndex() - 1);
					if (token.getType() == UnicodeIPAParser.WS) {
						// do nothing, trailing whitespace is trimmed by parser once parsing has completed
					} else if (token.getType() == UnicodeIPAParser.LIGATURE) {
						HangingLigatureException hle = new HangingLigatureException("");
						hle.setPositionInLine(token.getCharPositionInLine());
						ex = hle;
					} else {
						InvalidTokenException ete = new InvalidTokenException(e.getLocalizedMessage(), e);
						ete.setPositionInLine(token.getCharPositionInLine());
						ex = ete;
					}
				}
			} else if(e.getOffendingToken().getType() == UnicodeIPAParser.LIGATURE) {
				HangingLigatureException hle = new HangingLigatureException();
				hle.setPositionInLine(e.getOffendingToken().getCharPositionInLine());
				ex = hle;
			} else {
				InvalidTokenException ete = new InvalidTokenException(e.getLocalizedMessage(), e);
				ete.setPositionInLine(e.getOffendingToken().getCharPositionInLine());
				ex = ete;
			}
		} else {
			ex = new IPAParserException(e);
		}
		if(ex != null)
			throw ex;
	}
	
	@Override
	public Token recoverInline(Parser recognizer) throws RecognitionException {
		throw new IPAParserException("XXX");
	}
	
}
