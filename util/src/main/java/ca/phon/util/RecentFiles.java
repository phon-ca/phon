/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecentFiles implements Iterable<File> {
	
	private String propertyKey;
	
	private final static int DEFAULT_MAX_FILES = 10;
	
	private int maxFiles = 10;
	
	private List<File> fileHistory = new ArrayList<>();
	
	public RecentFiles() {
		super();
	}
	
	public RecentFiles(String propertyKey) {
		this(propertyKey, DEFAULT_MAX_FILES);
	}
	
	public RecentFiles(String propertyKey, int maxFiles) {
		super();
		setMaxFiles(maxFiles);
		setPropertyKey(propertyKey);
	}
	
	public void setMaxFiles(int maxFiles) {
		this.maxFiles = Math.max(0, maxFiles);
	}
	
	public int getMaxFiles() {
		return this.maxFiles;
	}
	
	public int getFileCount() {
		return fileHistory.size();
	}
	
	public File getFileAt(int idx) {
		return fileHistory.get(idx);
	}
	
	/**
	 * Remove any files which cannot be located from the
	 * history.
	 * 
	 */
	public void purgeFilesNotFound() {
		final Iterator<File> itr = iterator();
		while(itr.hasNext()) {
			final File f = itr.next();
			if(!f.exists())
				itr.remove();
		}
	}
	
	public void setPropertyKey(String key) {
		this.propertyKey = key;
		loadHistory();
	}
	
	public String getPropertyKey() {
		return this.propertyKey;
	}
	
	private void loadHistory() {
		String folderList = PrefHelper.get(propertyKey, "");
		final String folders[] = folderList.split(";");
		
		for(String folderName:folders) {
			if(folderName.trim().length() == 0) continue;
			final File folder = new File(folderName);
			fileHistory.add(0, folder);
		}
	}
	
	public void saveHistory() {
		final StringBuffer sb = new StringBuffer();
		
		int num = 0;
		for(int i = fileHistory.size()-1; i >= 0 && num++ < maxFiles; i--) {
			final File projectFolder = fileHistory.get(i);
			if(i < (fileHistory.size()-1)) sb.append(';');
			sb.append(projectFolder.getAbsolutePath());
		}
		
		PrefHelper.getUserPreferences().put(propertyKey, sb.toString());
	}
	
	public void addToHistory(File workspaceFolder) {
		if(fileHistory.contains(workspaceFolder))
			fileHistory.remove(workspaceFolder);
		fileHistory.add(0, workspaceFolder);
		while(fileHistory.size() > getMaxFiles()) {
			fileHistory.remove(fileHistory.size()-1);
		}
		saveHistory();
	}
	
	public void clearHistory() {
		fileHistory.clear();
		saveHistory();
	}

	@Override
	public Iterator<File> iterator() {
		return fileHistory.iterator();
	}
}
