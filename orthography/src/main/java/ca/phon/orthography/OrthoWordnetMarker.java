package ca.phon.orthography;

/**
 * Symbols used to create wordnets.
 */
public enum OrthoWordnetMarker {
	COMPOUND('+'),
	CLITIC('~');
	
	private char marker;
	
	private OrthoWordnetMarker(char c) {
		this.marker = c;
	}
	
	public char getMarker() {
		return this.marker;
	}
	
	public static OrthoWordnetMarker fromMarker(char c) {
		OrthoWordnetMarker retVal = null;
		
		for(OrthoWordnetMarker v:values()) {
			if(v.getMarker() == c) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
	
	@Override
	public String toString() {
		return "" + getMarker();
	}

}
