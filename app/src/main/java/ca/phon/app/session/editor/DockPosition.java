package ca.phon.app.session.editor;

/**
 * Dock positions for record editor.
 */
public enum DockPosition {
	// byte positions correspond to
	// t, l, b, r
	NORTH(100),
	EAST(200),
	SOUTH(100),
	WEST(200),
	CENTER(0);
	
	final int size;
	
	private DockPosition(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
}
