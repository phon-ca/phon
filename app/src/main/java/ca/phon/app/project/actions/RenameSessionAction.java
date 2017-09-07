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
import java.io.IOException;
import java.util.*;
import java.util.logging.*;

import ca.phon.app.project.*;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.CollatorFactory;

public class RenameSessionAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(RenameSessionAction.class.getName());

	private static final long serialVersionUID = 2062009179431805213L;

	public RenameSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Rename Session");
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
				showMessage("Rename Session", "You must specify a non-empty session name.");
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				showMessage("Rename Session", e.getLocalizedMessage());
				return;
			}
			
			UUID writeLock = null;
			try {
				writeLock = project.getSessionWriteLock(corpusName, newSessionName);
				project.saveSession(corpusName, newSessionName, session, writeLock);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				showMessage("Rename Session", e.getLocalizedMessage());
			} finally {
				if(writeLock != null) {
					try {
						project.releaseSessionWriteLock(corpusName, newSessionName, writeLock);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
					writeLock = null;
				}
			}
			
			try {
				writeLock = project.getSessionWriteLock(corpusName, sessionName);
				project.removeSession(corpusName, sessionName, writeLock);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				showMessage("Rename Session", e.getLocalizedMessage());
			} finally {
				if(writeLock != null) {
					try {
						project.releaseSessionWriteLock(corpusName, sessionName, writeLock);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
