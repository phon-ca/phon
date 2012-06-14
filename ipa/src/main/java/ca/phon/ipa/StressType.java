package ca.phon.ipa;

/**
 * Stress type
 */
public enum StressType {
	PRIMARY('\u02c8'),
	SECONDARY('\u02cc');
	
	private Character glyph;
	
	private StressType(Character glyph) {
		this.glyph = glyph;
	}
	
	public Character getGlyph() {
		return this.glyph;
	}
}