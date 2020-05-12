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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import ca.phon.app.project.NewSessionDialog;
import ca.phon.app.project.ProjectWindow;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.session.Session;

public class NewSessionAction extends ProjectWindowAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(NewSessionAction.class.getName());

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
			Session createdSession = proj.createSessionFromTemplate(corpusName, sessionName);
			sessionCreated = true;
			
			// setup media if available
			File mediaFile = MediaLocator.findMediaFile(sessionName, proj, corpusName);
			if(mediaFile != null && mediaFile.exists() && mediaFile.canRead()) {
				createdSession.setMediaLocation(mediaFile.getName());
				
				UUID wl = proj.getSessionWriteLock(createdSession);
				proj.saveSession(createdSession, wl);
				proj.releaseSessionWriteLock(createdSession, wl);
			}
			
			getWindow().refreshProject();
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			showMessage("New Session", e.getLocalizedMessage());
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
	
	public boolean isSessionCreated() {
		return this.sessionCreated;
	}

}
