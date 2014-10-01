package ca.phon.ipa;

/**
 * Intonation group type
 */
public enum IntonationGroupType {
	MAJOR('\u2016'),
	MINOR('\u007c');
	
	private Character glyph;
	
	private IntonationGroupType(Character glyph) {
		this.glyph = glyph;
	}
	
	public Character getGlyph() {
		return this.glyph;
	}
}
