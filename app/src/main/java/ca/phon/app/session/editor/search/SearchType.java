package ca.phon.app.session.editor.search;

public enum SearchType {
	PLAIN("Plain text"),
	REGEX("Regular expression"),
	PHONEX("Phonex");
	
	private String title;
	
	private SearchType(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return this.title;
	}
}
