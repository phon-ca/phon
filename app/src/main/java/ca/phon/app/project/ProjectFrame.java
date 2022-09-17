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

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

/**
 * <p>Common module frame with project extension.</p>
 * 
 * <p>Manual retrieval of the project data can be done
 * as shown below:</br>
 * 
 * <pre>
 * final ProjectFrameExtension pfe = frame.getExtension(ProjectFrameExtension.class);
 * if(pfe != null) return pfe.getProject();
 * </pre>
 *</p>
 */
public class ProjectFrame extends CommonModuleFrame {
	
	private static final long serialVersionUID = 4160918049917672755L;

	public ProjectFrame() {
		this(null);
	}
	
	public ProjectFrame(Project project) {
		super();
		setProject(project);
	}

	/**
	 * Get the project associated with the frame
	 * 
	 * @return project
	 */
	public Project getProject() {
		return getExtension(Project.class);
	}
	
	/**
	 * Set the project associated with the frame
	 * 
	 * @param project
	 */
	public void setProject(Project project) {
		putExtension(Project.class, project);
	}
	
	
}
