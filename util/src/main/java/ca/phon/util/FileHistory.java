/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.util;

import java.io.*;
import java.util.*;

public class FileHistory implements Iterable<File> {
	
	private final static int DEFAULT_MAX_FOLDERS = 10;

	public final String prop;

	public int maxFolders;
	
	private List<File> history = new ArrayList<>();

	private boolean removeDeadEntries = false;
		
	public FileHistory(String prop) {
		this(prop, DEFAULT_MAX_FOLDERS);
	}

	public FileHistory(String prop, int maxFolders) {
		this(prop, maxFolders, false);
	}

	public FileHistory(String prop, int maxFolders, boolean removeDeadEntries) {
		super();
		
		this.prop = prop;
		this.maxFolders = maxFolders;
		this.removeDeadEntries = removeDeadEntries;

		loadHistory();
	}

	/**
	 * Remove dead entries on load
	 *
	 * @return boolean
	 */
	public boolean isRemoveDeadEntries() {
		return this.removeDeadEntries;
	}

	public void setRemoveDeadEntries(boolean removeDeadEntries) {
		this.removeDeadEntries = removeDeadEntries;
	}
	
	private void loadHistory() {
		String folderList = PrefHelper.get(prop, "");
		final String folders[] = folderList.split(";");
		
		for(String folderName:folders) {
			if(folderName.trim().length() == 0) continue;
			if(history.size() >= maxFolders) break;
			final File folder = new File(folderName);
			if(isRemoveDeadEntries() && !folder.exists())
				continue;
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
