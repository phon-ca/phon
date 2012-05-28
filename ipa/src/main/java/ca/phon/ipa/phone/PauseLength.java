package ca.phon.ipa.phone;

/**
 * Pause lengths
 */
public enum PauseLength {
	SHORT("."),
	MEDIUM(".."),
	LONG("...");
	
	private String image;
	
	private PauseLength(String image) {
		this.image = image;
	}
	
	/**
	 * Length from string
	 * 
	 * @param text valid values must match the regex
	 *  <code>'\.{1,3}'</code>
	 * @return the detected PauseLength
	 * @throws IllegalArgumentException if the given text
	 *  is not a valid length string
	 */
	public static PauseLength lengthFromString(String text) {
		PauseLength retVal = null;
		if(!text.matches("\\.{1,3}")) {
			throw new IllegalArgumentException("Invalid length string '" + text + "'");
		}
		int len = text.length();
		switch(len) {
		case 1:
			retVal = SHORT;
			break;
			
		case 2:
			retVal = MEDIUM;
			break;
			
		case 3:
			retVal = LONG;
			break;
			
		default:
			throw new IllegalArgumentException("Invalid length string '" + text + "'");	
		}
		return retVal;
	}
	
	/**
	 * Get the length string for
	 * 
	 * @return the text for this length
	 */
	public String getText() {
		return this.image;
	}
}
