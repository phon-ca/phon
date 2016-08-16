package ca.phon.app.opgraph.report;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;

import ca.phon.util.PrefHelper;
import ca.phon.util.resources.FolderHandler;

/**
 * Read xml/opgraph files found in ~/Documents/Phon/reports/
 * 
 */
public class UserReportHandler extends FolderHandler<URL> {

	public final static String DEFAULT_USER_REPORT_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "reports";
	
	public UserReportHandler() {
		this(new File(DEFAULT_USER_REPORT_FOLDER));
	}
	
	public UserReportHandler(File file) {
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
