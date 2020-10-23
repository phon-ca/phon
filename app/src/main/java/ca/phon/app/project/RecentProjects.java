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
package ca.phon.app.project;

import java.io.*;
import java.util.*;

import ca.phon.util.*;

public class RecentProjects extends RecentFiles {
	
	public static final String PROJECT_HISTORY_PROP = RecentProjects.class.getName() + ".stack";
	
	public static final int MAX_PROJECTS = 10;
	
	private List<File> projectHistory = new ArrayList<>();
	
	public RecentProjects() {
		super(PROJECT_HISTORY_PROP, MAX_PROJECTS);
	}
	
}
