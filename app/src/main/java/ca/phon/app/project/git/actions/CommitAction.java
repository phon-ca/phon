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
package ca.phon.app.project.git.actions;

import java.awt.event.*;

import ca.phon.app.project.*;
import ca.phon.app.project.actions.*;
import ca.phon.app.project.git.*;

public class CommitAction extends ProjectWindowAction {

	private static final long serialVersionUID = 8240539097062235081L;

	public CommitAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Commit...");
		putValue(SHORT_DESCRIPTION, "Commit changes");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final GitCommitWizard wizard = new GitCommitWizard(getWindow().getProject());
		wizard.setParentFrame(getWindow());
		wizard.pack();
		wizard.setSize(600, wizard.getHeight());
		wizard.centerWindow();
		wizard.setVisible(true);
	}

}
