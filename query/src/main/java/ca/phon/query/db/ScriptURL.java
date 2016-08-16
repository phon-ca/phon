package ca.phon.query.db;

public class ScriptURL {
	
	private String path = "";
	
	private ScriptLibrary library = ScriptLibrary.OTHER;

	public ScriptURL() {
		super();
	}
	
	public ScriptURL(String path) {
		this(path, ScriptLibrary.OTHER);
	}
	
	public ScriptURL(String path, ScriptLibrary library) {
		super();
		
		this.path = path;
		this.library = library;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ScriptLibrary getLibrary() {
		return library;
	}

	public void setLibrary(ScriptLibrary library) {
		this.library = library;
	}
	
}
