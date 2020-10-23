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

import java.awt.event.*;
import java.io.*;
import java.util.*;

import ca.phon.app.project.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.ui.toast.*;
import ca.phon.util.*;

public class RenameSessionAction extends ProjectWindowAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(RenameSessionAction.class.getName());

	private static final long serialVersionUID = 2062009179431805213L;

	public RenameSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Rename session");
		putValue(SHORT_DESCRIPTION, "Rename selected session");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = getWindow().getProject();
		final String selectedCorpus = getWindow().getSelectedCorpus();
		final String selectedSession = getWindow().getSelectedSessionName();
		
		if(selectedCorpus == null || selectedSession == null) {
			ToastFactory.makeToast("Please select a session").start(getWindow().getSessionList());
			return;
		}
		
		final RenameSessionDialog dialog = new RenameSessionDialog(project, selectedCorpus, selectedSession);
		dialog.setModal(true);
		dialog.pack();
		dialog.setLocationRelativeTo(getWindow());
		dialog.setVisible(true);
		
		if(!dialog.wasCanceled()) {
			final String corpusName = dialog.getCorpus();
			final String sessionName = dialog.getSessionName();
			final String newSessionName = dialog.getNewSessionName();
			
			if (newSessionName == null || newSessionName.length() == 0) {
				showMessage("Rename Session", "Please enter session name.");
				return;
			}

			// Run through the sessions to see if the corpus specified exists, and
			// and also make sure that the new name isn't the name of an existing
			// corpus
			if (project.getCorpusSessions(corpusName).contains(newSessionName)) {
				showMessage("Rename Session", "A session with that name already exists.");
				return;
			}
			
			// Transfer XML data to the new session name
			Session session = null;
			try {
				session = project.openSession(corpusName, sessionName);
				session.setName(newSessionName);
			} catch(Exception e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				showMessage("Rename Session", e.getLocalizedMessage());
				return;
			}
			
			UUID writeLock = null;
			try {
				writeLock = project.getSessionWriteLock(corpusName, newSessionName);
				project.saveSession(corpusName, newSessionName, session, writeLock);
			} catch (Exception e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				showMessage("Rename Session", e.getLocalizedMessage());
			} finally {
				if(writeLock != null) {
					try {
						project.releaseSessionWriteLock(corpusName, newSessionName, writeLock);
					} catch (IOException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					}
					writeLock = null;
				}
			}
			
			try {
				writeLock = project.getSessionWriteLock(corpusName, sessionName);
				project.removeSession(corpusName, sessionName, writeLock);
			} catch (Exception e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				showMessage("Rename Session", e.getLocalizedMessage());
			} finally {
				if(writeLock != null) {
					try {
						project.releaseSessionWriteLock(corpusName, sessionName, writeLock);
					} catch (IOException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				}
			}
			
			// select new session
			final List<String> sessionNames = project.getCorpusSessions(corpusName);
			Collections.sort(sessionNames, CollatorFactory.defaultCollator());
			int idx = sessionNames.indexOf(newSessionName);
			if(idx >= 0) {
				getWindow().getSessionList().setSelectedIndex(idx);
			}
		}
	}

}
