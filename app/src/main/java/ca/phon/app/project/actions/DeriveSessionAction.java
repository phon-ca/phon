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
package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.mergewizard.DeriveSessionWizard;

public class DeriveSessionAction extends ProjectWindowAction {

	private static final long serialVersionUID = 4025880051460438742L;

	public DeriveSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Derive session...");
		putValue(SHORT_DESCRIPTION, "Create a new session from existing data");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final DeriveSessionWizard wizard = new DeriveSessionWizard(getWindow().getProject());
		wizard.setParentFrame(getWindow());
		wizard.setSize(600, 500);
		wizard.setLocationByPlatform(true);
		wizard.setVisible(true);
	}

}
