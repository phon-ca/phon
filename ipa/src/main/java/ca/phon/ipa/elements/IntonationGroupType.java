package ca.phon.ipa.elements;

/**
 * Intonation group type
 */
public enum IntonationGroupType {
	MAJOR('\u0000'),
	MINOR('\u0000');
	
	private Character glyph;
	
	private IntonationGroupType(Character glyph) {
		this.glyph = glyph;
	}
	
	public Character getGlyph() {
		return this.glyph;
	}
}
