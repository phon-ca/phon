package ca.phon.app.opgraph.macro;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;

import ca.phon.util.PrefHelper;
import ca.phon.util.resources.FolderHandler;

public class UserMacroHandler extends FolderHandler<URL> {

	public final static String DEFAULT_USER_MACRO_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "macro";
	
	public UserMacroHandler() {
		this(new File(DEFAULT_USER_MACRO_FOLDER));
	}
	
	public UserMacroHandler(File file) {
		super(file);
		setRecursive(true);
		super.setFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return (pathname.getName().endsWith(".xml") ||
						pathname.getName().endsWith(".opgraph"));
			}
		});
	}

	@Override
	public URL loadFromFile(File f) throws IOException {
		return (f != null ? f.toURI().toURL() : null);
	}
	
}
