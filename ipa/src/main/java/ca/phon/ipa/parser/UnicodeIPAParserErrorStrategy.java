package ca.phon.ipa.parser;

import ca.phon.ipa.WordBoundary;
import ca.phon.ipa.parser.exceptions.HangingLigatureException;
import ca.phon.ipa.parser.exceptions.IPAParserException;
import ca.phon.ipa.parser.exceptions.InvalidTokenException;
import ca.phon.ipa.parser.exceptions.StrayDiacriticException;
import org.antlr.v4.runtime.*;

public class UnicodeIPAParserErrorStrategy extends DefaultErrorStrategy {

	private UnicodeIPAParserListener listener;

	public UnicodeIPAParserErrorStrategy(UnicodeIPAParserListener listener) {
		this.listener = listener;
	}

	@Override
	public void reportError(Parser recognizer,
	                        RecognitionException e) {
		// do nothing
	}

	@Override
	public void recover(Parser recognizer, RecognitionException e) {
		IPAParserException ex = null;
		if (e.getOffendingToken().getType() == CommonToken.EOF
			|| e.getOffendingToken().getType() == UnicodeIPAParser.WS) {
			// check previous token
			if (e.getOffendingToken().getTokenIndex() > 0) {
				Token token = recognizer.getInputStream().get(e.getOffendingToken().getTokenIndex() - 1);
				if (token.getType() == UnicodeIPAParser.WS) {
					// do nothing, trailing whitespace is trimmed by parser once parsing has completed
				} else if (token.getType() == UnicodeIPAParser.LIGATURE) {
					HangingLigatureException hle = new HangingLigatureException("Ligature missing right-hand element");
					hle.setPositionInLine(token.getCharPositionInLine());
					ex = hle;
				} else if(token.getType() == UnicodeIPAParser.PREFIX_DIACRITIC
					|| token.getType() == UnicodeIPAParser.SUFFIX_DIACRITIC
					|| token.getType() == UnicodeIPAParser.COMBINING_DIACRITIC
					|| token.getType() == UnicodeIPAParser.TONE
					|| token.getType() == UnicodeIPAParser.TONE_NUMBER
					|| e.getOffendingToken().getType() == UnicodeIPAParser.LONG
					|| e.getOffendingToken().getType() == UnicodeIPAParser.HALF_LONG) {
					StrayDiacriticException sde = new StrayDiacriticException("Stray diacritic");
					sde.setPositionInLine(token.getCharPositionInLine());
					ex = sde;
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
		} else if(e.getOffendingToken().getType() == UnicodeIPAParser.PREFIX_DIACRITIC
				|| e.getOffendingToken().getType() == UnicodeIPAParser.SUFFIX_DIACRITIC
				|| e.getOffendingToken().getType() == UnicodeIPAParser.COMBINING_DIACRITIC
				|| e.getOffendingToken().getType() == UnicodeIPAParser.TONE
				|| e.getOffendingToken().getType() == UnicodeIPAParser.TONE_NUMBER
				|| e.getOffendingToken().getType() == UnicodeIPAParser.LONG
				|| e.getOffendingToken().getType() == UnicodeIPAParser.HALF_LONG) {
			StrayDiacriticException sde = new StrayDiacriticException("Stray diacritic");
			sde.setPositionInLine(e.getOffendingToken().getCharPositionInLine());
			ex = sde;
		} else {
			InvalidTokenException ete = new InvalidTokenException(e.getLocalizedMessage(), e);
			ete.setPositionInLine(e.getOffendingToken().getCharPositionInLine());
			ex = ete;
		}
		if(ex != null)
			throw ex;
	}
	
	@Override
	public Token recoverInline(Parser recognizer) throws RecognitionException {
		// don't recover
		throw new InputMismatchException(recognizer);
	}
	
}
