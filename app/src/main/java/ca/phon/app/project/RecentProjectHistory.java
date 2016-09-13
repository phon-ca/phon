package ca.phon.app.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.phon.util.PrefHelper;

public class RecentProjectHistory implements Iterable<File> {
	
	public static final String PROJECT_HISTORY_PROP = RecentProjectHistory.class.getName() + ".stack";
	
	public static final int MAX_PROJECTS = 10;
	
	private List<File> projectHistory = new ArrayList<>();
	
	public RecentProjectHistory() {
		super();
		loadHistory();
	}
	
	private void loadHistory() {
		String folderList = PrefHelper.get(PROJECT_HISTORY_PROP, "");
		final String folders[] = folderList.split(";");
		
		for(String folderName:folders) {
			if(folderName.trim().length() == 0) continue;
			final File folder = new File(folderName);
			projectHistory.add(0, folder);
		}
	}
	
	public void saveHistory() {
		final StringBuffer sb = new StringBuffer();
		
		int num = 0;
		for(int i = projectHistory.size()-1; i >= 0 && num++ < MAX_PROJECTS; i--) {
			final File projectFolder = projectHistory.get(i);
			if(i < (projectHistory.size()-1)) sb.append(';');
			sb.append(projectFolder.getAbsolutePath());
		}
		
		PrefHelper.getUserPreferences().put(PROJECT_HISTORY_PROP, sb.toString());
	}
	
	public void addToHistory(File workspaceFolder) {
		if(projectHistory.contains(workspaceFolder))
			projectHistory.remove(workspaceFolder);
		projectHistory.add(0, workspaceFolder);
		saveHistory();
	}
	
	public void clearHistory() {
		projectHistory.clear();
		saveHistory();
	}

	@Override
	public Iterator<File> iterator() {
		return projectHistory.iterator();
	}
}
