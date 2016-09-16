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
package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.project.NewSessionDialog;
import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;

public class NewSessionAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(NewSessionAction.class.getName());

	private static final long serialVersionUID = 3077154531739507863L;
	
	private String corpus;
	
	private String sessionName;
	
	private boolean sessionCreated = false;

	public NewSessionAction(ProjectWindow projectWindow) {
		this(projectWindow, null, null);
	}
	
	public NewSessionAction(ProjectWindow projectWindow, String corpus) {
		this(projectWindow, corpus, null);
	}
	
	public NewSessionAction(ProjectWindow projectWindow, String corpus, String sessionName) {
		super(projectWindow);
		
		this.corpus = corpus;
		this.sessionName = sessionName;
		
		putValue(NAME, "New Session...");
		putValue(SHORT_DESCRIPTION, "Add new session to project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project proj = getWindow().getProject();
		String corpusName = 
				(this.corpus == null ? getWindow().getSelectedCorpus() : this.corpus);
		String sessionName = this.sessionName;
		
		if(sessionName == null) {
			NewSessionDialog dlg = null;
			if(getWindow().getSelectedCorpus() == null) {
				dlg = new NewSessionDialog(proj);
			} else {
				dlg = new NewSessionDialog(proj, corpusName);
			}
			dlg.setModal(true);
			dlg.pack();
			dlg.setVisible(true);
			
			if(!dlg.wasCanceled()) {
				corpusName = dlg.getCorpusName();
				sessionName = dlg.getSessionName();
			}
		}
		
		// create session
		try {
			proj.createSessionFromTemplate(corpusName, sessionName);
			sessionCreated = true;
			getWindow().refreshProject();
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			showMessage("New Session", e.getLocalizedMessage());
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public boolean isSessionCreated() {
		return this.sessionCreated;
	}

}
