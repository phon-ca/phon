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
package ca.phon.app.workspace;

import ca.phon.util.RecentFiles;

import java.io.File;
import java.util.Iterator;

public class WorkspaceHistory extends RecentFiles {
	
	public static final String WORKSPACE_HISTORY_PROP = WorkspaceHistory.class.getName() + ".stack";
	
	private final static int MAX_FOLDERS = 10;

	public WorkspaceHistory() {
		super(WORKSPACE_HISTORY_PROP, MAX_FOLDERS, true);
	}

	@Override
	public void purgeFilesNotFound() {
		final Iterator<File> itr = iterator();
		while(itr.hasNext()) {
			final File f = itr.next();
			if(!f.equals(Workspace.userWorkspaceFolder()) && !f.exists())
				itr.remove();
		}
	}
}
