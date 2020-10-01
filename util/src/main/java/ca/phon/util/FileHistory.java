package ca.phon.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.phon.util.PrefHelper;

public class FileHistory implements Iterable<File> {
	
	private final static int DEFAULT_MAX_FOLDERS = 10;

	public final String prop;

	public int maxFolders;
	
	private List<File> history = new ArrayList<>();
		
	public FileHistory(String prop) {
		this(prop, DEFAULT_MAX_FOLDERS);
	}
	
	public FileHistory(String prop, int maxFolders) {
		super();
		
		this.prop = prop;
		this.maxFolders = maxFolders;
		
		loadHistory();
	}
	
	private void loadHistory() {
		String folderList = PrefHelper.get(prop, "");
		final String folders[] = folderList.split(";");
		
		for(String folderName:folders) {
			if(folderName.trim().length() == 0) continue;
			if(history.size() >= maxFolders) break;
			final File folder = new File(folderName);
			history.add(0, folder);
		}
	}
	
	public void saveHistory() {
		final StringBuffer sb = new StringBuffer();
		
		int num = 0;
		for(int i = history.size()-1; i >= 0 && num++ < maxFolders; i--) {
			final File workspaceFolder = history.get(i);
			if(i < (history.size()-1)) sb.append(';');
			sb.append(workspaceFolder.getAbsolutePath());
		}
		
		PrefHelper.getUserPreferences().put(prop, sb.toString());
	}
	
	public void addToHistory(File workspaceFolder) {
		if(history.contains(workspaceFolder))
			history.remove(workspaceFolder);
		history.add(0, workspaceFolder);
		saveHistory();
	}
	
	public void clearHistory() {
		history.clear();
		saveHistory();
	}
	
	public int size() {
		return history.size();
	}
	
	@Override
	public Iterator<File> iterator() {
		return history.iterator();
	}
	
}
