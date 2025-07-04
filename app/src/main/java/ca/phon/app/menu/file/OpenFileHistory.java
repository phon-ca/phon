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
package ca.phon.app.menu.file;

import ca.phon.util.PrefHelper;
import ca.phon.util.RecentFiles;

public class OpenFileHistory extends RecentFiles {

	public final static String OPENFILE_HISTORY_PROP = OpenFileHistory.class.getName() + ".openFileHistory";
	
	private final static int DEFAULT_MAX_OPENFILES = 10;
	public final static String OPENFILE_HISTORY_MAXFILES = OpenFileHistory.class.getName() + ".maxFiles";
	
	public OpenFileHistory() {
		super(OPENFILE_HISTORY_PROP, PrefHelper.getInt(OPENFILE_HISTORY_MAXFILES, DEFAULT_MAX_OPENFILES));
	}

}
