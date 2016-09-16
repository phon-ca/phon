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
