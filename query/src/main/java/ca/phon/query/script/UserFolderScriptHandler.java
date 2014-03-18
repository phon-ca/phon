package ca.phon.query.script;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import ca.phon.util.resources.FolderHandler;

public class UserFolderScriptHandler extends FolderHandler<QueryScript> {

	public UserFolderScriptHandler(File folder) {
		super(folder);
		setFileFilter(scriptFilter);
		setRecursive(true);
	}
	
	@Override
	public QueryScript loadFromFile(File f) throws IOException {
		return new QueryScript(f.toURI().toURL());
	}

	private final FileFilter scriptFilter = new FileFilter() {
		@Override
		public boolean accept(File f) {
			final String name = f.getAbsolutePath();
			boolean prefixOk = 
					!(name.startsWith(".") || name.startsWith("~") || name.startsWith("__"));
			boolean suffixOk = 
					(name.endsWith(".js") || name.endsWith(".xml"));
			return prefixOk && suffixOk;
		}
	};
	
}
