package ca.phon.app.menu.file;

import ca.phon.util.FileHistory;
import ca.phon.util.PrefHelper;

public class OpenFileHistory extends FileHistory {

	public final static String OPENFILE_HISTORY_PROP = OpenFileHistory.class.getName() + ".openFileHistory";
	
	private final static int DEFAULT_MAX_OPENFILES = 10;
	public final static String OPENFILE_HISTORY_MAXFILES = OpenFileHistory.class.getName() + ".maxFiles";
	
	public OpenFileHistory() {
		super(OPENFILE_HISTORY_PROP, PrefHelper.getInt(OPENFILE_HISTORY_MAXFILES, DEFAULT_MAX_OPENFILES));
	}

}
