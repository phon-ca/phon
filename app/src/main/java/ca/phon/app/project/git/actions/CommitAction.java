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
package ca.phon.app.project.git.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.GitCommitWizard;

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
