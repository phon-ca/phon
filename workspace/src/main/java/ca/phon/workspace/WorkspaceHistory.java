package ca.phon.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.phon.util.PrefHelper;

public class WorkspaceHistory implements Iterable<File> {
	
	public static final String WORKSPACE_HISTORY_PROP = WorkspaceHistory.class.getName() + ".stack";
	
	private final static int MAX_FOLDERS = 10;
	
	private List<File> workspaceHistory = new ArrayList<>();
	
	public WorkspaceHistory() {
		super();
		
		loadHistory();
	}
	
	private void loadHistory() {
		String folderList = PrefHelper.get(WORKSPACE_HISTORY_PROP, Workspace.defaultWorkspaceFolder().getAbsolutePath());
		if(folderList.trim().length() == 0) folderList = Workspace.defaultWorkspaceFolder().getAbsolutePath();
		final String folders[] = folderList.split(";");
		
		for(String folderName:folders) {
			if(folderName.trim().length() == 0) continue;
			final File folder = new File(folderName);
			workspaceHistory.add(0, folder);
		}
	}
	
	public void saveHistory() {
		final StringBuffer sb = new StringBuffer();
		
		int num = 0;
		for(int i = workspaceHistory.size()-1; i >= 0 && num++ < MAX_FOLDERS; i--) {
			final File workspaceFolder = workspaceHistory.get(i);
			if(i < (workspaceHistory.size()-1)) sb.append(';');
			sb.append(workspaceFolder.getAbsolutePath());
		}
		
		PrefHelper.getUserPreferences().put(WORKSPACE_HISTORY_PROP, sb.toString());
	}
	
	public void addToHistory(File workspaceFolder) {
		if(workspaceHistory.contains(workspaceFolder))
			workspaceHistory.remove(workspaceFolder);
		workspaceHistory.add(0, workspaceFolder);
		saveHistory();
	}
	
	public void clearHistory() {
		workspaceHistory.clear();
		saveHistory();
	}
	
	@Override
	public Iterator<File> iterator() {
		return workspaceHistory.iterator();
	}
	
}
