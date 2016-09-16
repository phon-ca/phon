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
package ca.phon.app.autosave;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import ca.phon.project.Project;
import ca.phon.session.Session;

/**
 * Extension for {@link Project}s which manages auto-save files for 
 * a project.  This extension is automatically attacthed to project
 * files when available in the classpath.
 * 
 */
public class Autosaves {
	
	/**
	 * Project reference
	 */
	private final WeakReference<Project> projectRef;
	
	public Autosaves(Project project) {
		super();
		this.projectRef = new WeakReference<Project>(project);
	}
	
	public String getAutosavePath(Session session) {
		return getAutosavePath(session.getCorpus(), session.getName());
	}
	
	/**
	 * Get the autosave path for the given session.
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return autosave path
	 */
	public String getAutosavePath(String corpus, String session) {
		final File projectFolder = new File(getProject().getLocation());
		final File corpusFolder = new File(projectFolder, corpus);
		final File autosaveFile = new File(corpusFolder, AutosaveManager.AUTOSAVE_PREFIX + session + ".xml");
		return autosaveFile.getAbsolutePath();
	}
	
	public boolean hasAutosave(Session session) {
		return hasAutosave(session.getCorpus(), session.getName());
	}
	
	/**
	 * Does the project have an autosave for the given session.
	 * 
	 * @param corpus
	 * @param session
	 */
	public boolean hasAutosave(String corpus, String session) {
		final File autosaveFile = new File(getAutosavePath(corpus, session));
		return (autosaveFile.exists() && !autosaveFile.isDirectory());
	}
	
	public void createAutosave(Session session) throws IOException {
		createAutosave(session, session.getCorpus(), session.getName());
	}
	
	/**
	 * Create an autosave for the givn sesion.
	 * 
	 * @param session
	 * @throws IOException
	 */
	public void createAutosave(Session session, String corpus, String sessionName) throws IOException {
		final Project project = getProject();
		final String autosaveName = AutosaveManager.AUTOSAVE_PREFIX + sessionName;
		final UUID writeLock = project.getSessionWriteLock(corpus, autosaveName);
		project.saveSession(corpus, autosaveName, session, writeLock);
		project.releaseSessionWriteLock(corpus, autosaveName, writeLock);
	}
	
	public LocalDateTime getAutosaveDateTime(Session session) {
		return getAutosaveDateTime(session.getCorpus(), session.getName());
	}
	
	/**
	 * Get the creation date of the autosave file for a session.
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return session modification date, <code>null</code> if autosave does not exist
	 */
	public LocalDateTime getAutosaveDateTime(String corpus, String session) {
		if(!hasAutosave(corpus, session)) return null;
		
		final File autosaveFile = new File(getAutosavePath(corpus, session));
		final long autosaveModified = autosaveFile.lastModified();
		
		final LocalDateTime retVal = LocalDateTime.ofEpochSecond(autosaveModified, 0, ZoneOffset.UTC);
		return retVal;
	}
	
	public Session openAutosave(Session session) throws IOException {
		return openAutosave(session.getCorpus(), session.getName());
	}
	
	/**
	 * Open session from autosave file.
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return session
	 * 
	 */
	public Session openAutosave(String corpus, String session) throws IOException {
		final String autosaveName = AutosaveManager.AUTOSAVE_PREFIX + session;
		
		Session autosaveSession = getProject().openSession(corpus, autosaveName);
		autosaveSession.setName(session); // reset name in session object
		return autosaveSession;
	}
	
	/**
	 * Get the project refernce.
	 * 
	 * @return project
	 */
	public Project getProject() {
		return this.projectRef.get();
	}
	

}
