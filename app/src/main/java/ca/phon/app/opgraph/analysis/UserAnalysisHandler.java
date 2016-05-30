package ca.phon.app.opgraph.analysis;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;

import ca.phon.util.PrefHelper;
import ca.phon.util.resources.FolderHandler;

public class UserAnalysisHandler extends FolderHandler<URL> {

	public final static String DEFAULT_USER_ANALYSIS_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "analysis";
	
	public UserAnalysisHandler() {
		this(new File(DEFAULT_USER_ANALYSIS_FOLDER));
	}
	
	public UserAnalysisHandler(File file) {
		super(file);
		
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
