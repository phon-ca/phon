package ca.phon.app.session.editor;

/*
 * View categories
 */
public enum EditorViewCategory {
	SESSION("Session"),
	RECORD("Record"),
	MEDIA("Media"),
	QUERY("Search"),
	UTILITIES("Utilities"),
	PLUGINS("Plugins");
	
	String title;
	
	private EditorViewCategory(String title) {
		this.title = title;
	}
	
}
