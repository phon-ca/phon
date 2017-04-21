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
package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.mergewizard.DeriveSessionWizard;

public class DeriveSessionAction extends ProjectWindowAction {

	private static final long serialVersionUID = 4025880051460438742L;

	public DeriveSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Derive Session...");
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
