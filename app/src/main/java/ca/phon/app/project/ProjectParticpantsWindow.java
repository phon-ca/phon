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

import java.awt.*;

import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.decorations.*;

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
