package ca.phon.orthography;

/**
 * Punctuation support in orthography.
 */
public enum OrthoPunctType {
	PERIOD('.'),
	COMMA(','),
	EXCLAMATION('!'),
	QUESTION('?'),
	AT('@'),
	HASH('#'),
	DOLLARSIGN('$'),
	PERCENT('%'),
	CARET('^'),
	AMPERSTAND('&'),
	OPEN_BRACE('{'),
	CLOSE_BRACE('}'),
	FORWARD_SLASH('/'),
	BACK_SLASH('\\');
	
	private final char punctChar;
	
	private OrthoPunctType(char c) {
		this.punctChar = c;
	}
	
	public char getChar() {
		return this.punctChar;
	}
	
	public static OrthoPunctType fromChar(char c) {
		OrthoPunctType retVal = null;
		
		for(OrthoPunctType v:values()) {
			if(v.getChar() == c) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
	
}
