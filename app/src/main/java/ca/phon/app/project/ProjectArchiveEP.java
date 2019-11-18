/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.io.File;
import java.time.LocalDate;
import java.util.Map;

import ca.phon.app.welcome.ProjectArchiveTask;
import ca.phon.app.workspace.Workspace;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.format.DateFormatter;

@PhonPlugin(name="default")
public class ProjectArchiveEP implements IPluginEntryPoint {
	
	/**
	 * The project we are archiving
	 */
	private Project project;

	private final static String EP_NAME = "ProjectArchive";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		//make sure we have a project
		if(initInfo.get("project") == null) {
			throw new IllegalArgumentException("project cannot be null");
		}
		
		Object v = initInfo.get("project");
		if(!(v instanceof Project)) {
			throw new IllegalArgumentException("project object does not implement IPhonProject interface");
		}
		project = (Project)v;
		
		// display options UI
		
		// default output file
//		PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
//		String today = pdf.format(Calendar.getInstance());
		final String today = DateFormatter.dateTimeToString(LocalDate.now());
		
		String zipFileName = 
			project.getName() + "-" + today + ".zip";
		File archiveDir = 
			new File(Workspace.userWorkspace().getWorkspaceFolder(), "archives");
		if(!archiveDir.exists()) {
			archiveDir.mkdirs();
		}
		
		File zipFile =
			new File(archiveDir, zipFileName);
		
		// create and execute task
		final ProjectArchiveTask task = new ProjectArchiveTask(project, zipFile);
		task.performTask();
		
	}
}
