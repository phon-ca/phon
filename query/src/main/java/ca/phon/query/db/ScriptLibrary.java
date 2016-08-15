package ca.phon.query.db;

public enum ScriptLibrary {
	STOCK, // QueryScriptLibrary.stockScriptFiles
	USER, // QueryScriptLibrary.userScriptFiles
	PROJECT, // QueryScriptLibrary.projectScriptFiles(Project)
	PLUGINS,
	OTHER; // absolute
}
