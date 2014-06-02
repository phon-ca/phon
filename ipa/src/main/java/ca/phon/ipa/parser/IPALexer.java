package ca.phon.ipa.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

import ca.phon.syllable.SyllableConstituentType;


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
	 * Set to true when the next token expected is a 
	 * syllable constituent type identifier
	 */
	private boolean expectingScType = false;
	
	private boolean expectingGroupReference = false;
	
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

	/**
	 * @return
	 */
	@Override
	public Token nextToken() {
		Token retVal = null;
		
		while(retVal == null && currentPosition < source.length()) {
			char currentChar = source.charAt(currentPosition);
			IPATokenType tokenType = tokenMapper.getTokenType(currentChar);
			
			if(expectingScType) {
				final SyllableConstituentType scType = SyllableConstituentType.fromString(currentChar+"");
				if(scType == null) {
					IPAParserException ex = new IPAParserException("Invalid syllable constituent type '" +
							currentChar + "'");
					ex.setPositionInLine(currentPosition);
					reportError(ex);
				} else {
					int antlrType = tokenMapper.getTypeValue(IPATokenType.SCTYPE);
					retVal = new CommonToken(antlrType, currentChar+"");
					retVal.setCharPositionInLine(currentPosition);
				}
				expectingScType = false;
			} else if(expectingGroupReference) {
				final StringBuffer buffer = new StringBuffer();
				int startPos = currentPosition;
				while(tokenType != IPATokenType.CLOSE_BRACE && currentPosition < source.length()) {
					buffer.append(currentChar);
					currentChar = source.charAt(++currentPosition);
					tokenType = tokenMapper.getTokenType(currentChar);
				}
				
				int antlrType = tokenMapper.getTypeValue(IPATokenType.GROUP_NAME);
				retVal = new CommonToken(antlrType, buffer.toString());
				retVal.setCharPositionInLine(startPos);
				
				expectingGroupReference = false;
				
				// returrn close brace as next token
				if(tokenType == IPATokenType.CLOSE_BRACE)
					--currentPosition;
			} else {
				if(tokenType == null) {
					IPAParserException ex = new IPAParserException("Invalid token '" + currentChar + "'");
					ex.setPositionInLine(currentPosition);
					reportError(ex);
				} else {
					int antlrType = tokenMapper.getTypeValue(tokenType);
					
					retVal = new CommonToken(antlrType, currentChar+"");
					retVal.setCharPositionInLine(currentPosition);
					
					if(tokenType == IPATokenType.COLON) {
						expectingScType = true;
					} else if(tokenType == IPATokenType.OPEN_BRACE) {
						expectingGroupReference = true;
					}
				}
				
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
