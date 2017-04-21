/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.BorderLayout;

import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;

/**
 * Window for viewing and modifying {@link Participant}s for a {@link Project}.
 */
public class ProjectParticpantsWindow extends CommonModuleFrame {
	
	private DialogHeader header;
	
	private ParticipantsPanel participantsPanel;
	
	public ProjectParticpantsWindow(Project project) {
		super("Project Participants");
		
		putExtension(Project.class, project);
		init();
	}
	
	public Project getProject() {
		return getExtension(Project.class);
	}

	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("Project Participants", "View and modify participants for the project");
		add(header, BorderLayout.NORTH);
		
		participantsPanel = new ParticipantsPanel(getProject());
		add(participantsPanel, BorderLayout.CENTER);
	}
	
}
