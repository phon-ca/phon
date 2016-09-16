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
