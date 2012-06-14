package ca.phon.ipa.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;


/**
 * <p>Tokenize IPA strings for an ANTLR parser.  The 
 * {@link #next()} method will not throw an exception,
 * however a</p>
 * 
 * 
 */
public class IPALexer implements TokenSource {

	/**
	 * Token mapper
	 * 
	 */
	private IPATokens tokenMapper;
	
	/**
	 * Source string
	 * 
	 */
	private String source;
	
	/**
	 * Current position
	 */
	private int currentPosition = 0;
	
	/**
	 * Error handlers
	 */
	private List<IPAParserErrorHandler> errorHandlers = 
			Collections.synchronizedList(new ArrayList<IPAParserErrorHandler>());
	
	/**
	 * Constructor
	 * 
	 * @param string the string to tokenize
	 */
	public IPALexer(String string) {
		this.source = string;
		this.currentPosition = 0;
		
		tokenMapper = IPATokens.getSharedInstance();
	}
	
	/**
	 * Add an error handler to the lexer
	 * 
	 * @param handler
	 */
	public void addErrorHandler(IPAParserErrorHandler handler) {
		if(!errorHandlers.contains(handler)) {
			errorHandlers.add(handler);
		}
	}
	
	/**
	 * Remove an error handler from the lexer
	 * 
	 * @param handler
	 */
	public void removeErrorHandler(IPAParserErrorHandler handler) {
		errorHandlers.remove(handler);
	}
	
	/**
	 * Report an error to all handlers
	 * 
	 * @param ex
	 */
	private void reportError(IPAParserException ex) {
		for(IPAParserErrorHandler handler:errorHandlers) {
			handler.handleError(ex);
		}
	}

	@Override
	public Token nextToken() {
		Token retVal = null;
		
		while(retVal == null && currentPosition < source.length()) {
			char currentChar = source.charAt(currentPosition);
			
			IPATokenType tokenType = tokenMapper.getTokenType(currentChar);
			if(tokenType == null) {
				IPAParserException ex = new IPAParserException("Invalid token '" + currentChar + "'");
				ex.setPositionInLine(currentPosition);
				reportError(ex);
			} else {
				int antlrType = tokenMapper.getTypeValue(tokenType);
				
				retVal = new CommonToken(antlrType, currentChar+"");
				retVal.setCharPositionInLine(currentPosition);
			}
			currentPosition++;
		}
		
		if(retVal == null) {
			retVal = Token.EOF_TOKEN;
		}
		
		return retVal;
	}

	@Override
	public String getSourceName() {
		return "";
	}

}
