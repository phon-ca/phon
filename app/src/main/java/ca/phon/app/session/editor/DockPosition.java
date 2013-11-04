package ca.phon.app.session.editor;

/**
 * Dock positions for record editor.
 */
public enum DockPosition {
	// byte positions correspond to
	// t, l, b, r
	NORTH(0.33f),
	EAST(0.33f),
	SOUTH(0.33f),
	WEST(0.33f),
	CENTER(0.66f);
	
	final float size;
	
	private DockPosition(float size) {
		this.size = size;
	}
	
	public float getSize() {
		return size;
	}
}
