package ca.phon.ipa.phone.parser;

/**
 * Interface for implementing error handlers for phone
 * lexing/parsing.
 */
public interface PhoneParserErrorHandler {
	
	/**
	 * Handle a lexing/parser error.  This method recieves the error
	 * and lexing/parsring will attempt to continue.  You can halt the
	 * process by throwing a {@link RuntimeException}.
	 * 
	 * @param ex
	 */
	public void handleError(PhoneParserException ex);

}
