/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ca.phon.extensions.IExtendable;
import ca.phon.session.Session;

/**
 * Interface for a phon project.
 * Projects are responsible for corpus and session
 * lists as well as managing serialization for
 * sessions.
 * 
 */
public interface Project extends IExtendable {
	
	/*
	 * Listeners
	 */
	public List<ProjectListener> getProjectListeners();
	
	public void addProjectListener(ProjectListener listener);
	
	public void removeProjectListener(ProjectListener listener);
	
	/*
	 * Events
	 */
	public void fireProjectStructureChanged(ProjectEvent pe);
	
	public void fireProjectDataChanged(ProjectEvent pe);
	
	public void fireProjectWriteLocksChanged(ProjectEvent pe);
	
	/**
	 * Project version
	 *
	 * @return the project version or 'unk' if not known
	 */
	public String getVersion();
	
	/**
	 * The location of the project.
	 * 
	 * @return the project location
	 */
	public String getLocation();

	/**
	 * The name of the project.
	 * 
	 * @return project name
	 */
	public String getName();
	
	/**
	 * Set project name
	 * 
	 * @param name must match pattern '[ \w\d-]+'
	 */
	public void setName(String name);
	
	/**
	 * Project UUID
	 * 
	 * UUID for the project
	 * 
	 * @return uuid
	 */
	public UUID getUUID();
	
	/**
	 * Set project UUID
	 * 
	 * @param uuid
	 */
	public void setUUID(UUID uuid);
	
	/**
	 * Get the list of corpora in this project.  Corpus names
	 * are returned in alphabetical order.
	 * 
	 * @return list of corpora
	 */
	public List<String> getCorpora();
	
	/**
	 * Add a new corpus with the specified name.
	 * 
	 * @param corpus
	 * @param description
	 * @throws IOException if the corpus could not be
	 *  created
	 */
	public void addCorpus(String name, String description)
		throws IOException;
	
	/**
	 * Rename a corpus
	 * 
	 * @param corpus
	 * @param newName
	 * 
	 * @throws IOException if the corpus could not be
	 *  renamed
	 */
	public void renameCorpus(String corpus, String newName) 
		throws IOException;
	
	/**
	 * Delete the specified corpus and all sessions it contains.
	 * 
	 * @param corpus
	 * 
	 * @throws IOException if the corpus could not be deleted
	 */
	public void removeCorpus(String corpus)
		throws IOException;
	
	/**
	 * Get the description of the specified corpus.
	 * 
	 * @param corpus the corpus name
	 */
	public String getCorpusDescription(String corpus);
	
	/**
	 * Set the description for the specified corpus.
	 * 
	 * @param corpus
	 * @param description
	 */
	public void setCorpusDescription(String corpus, String description);
	
	/**
	 * Get the Session template for the given corpus.
	 * 
	 * @param corpus
	 * 
	 * @return session template or <code>null</code> if not found
	 * @throws IOException
	 */
	public Session getSessionTemplate(String corpus)
		throws IOException;
	
	/**
	 * Save the Session template for the given corpus.
	 * 
	 * @param corpus
	 * @param template
	 * 
	 * @throws IOException
	 */
	public void saveSessionTemplate(String corpus, Session template) 
			throws IOException;
	
	/**
	 * Create a new session from the corpus template (if it exists)
	 * This method will also add the session to the specified corpus.
	 * 
	 * @param corpus
	 * @param sessionName
	 * 
	 * @return new Session object
	 */
	public Session createSessionFromTemplate(String corpus, String session)
		throws IOException;
	
	/**
	 * Get the session names contained in a corpus in alphabetical
	 * order.
	 * 
	 * @param corpus
	 * 
	 * @return the list of sessions in the specified corpus
	 */
	public List<String> getCorpusSessions(String corpus);
	
	/**
	 * Return the path to the given corpus.
	 * 
	 * @param corpus
	 */
	public String getCorpusPath(String corpus);
	
	/**
	 * Returns the number of records in a session w/o opening
	 * the session. This method is faster than using
	 * openSession(corpus, session).numberOfRecords()
	 * 
	 * @param session
	 * @return number of records in the session
	 * @throws IOException
	 */
	public int numberOfRecordsInSession(String corpus, String session)
		throws IOException;
	
	/**
	 * Open the specified session.  This will create a new session
	 * object with the data currently on the storage device.
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return the session
	 * 
	 * @throws IOException
	 */
	public Session openSession(String corpus, String session)
		throws IOException;
	
	/**
	 * Get path to the given session.
	 * 
	 * @param session
	 * 
	 * @return path to given session
	 */
	public String getSessionPath(Session session);
	
	/**
	 * Get path to the given session.
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return path to given session
	 */
	public String getSessionPath(String corpus, String session);
	
	/**
	 * Get a write lock for a session.  Before writing a write lock
	 * must be obtained from the project.  
	 * 
	 * @param session
	 * 
	 * @return the session write lock or < 0 if a write lock
	 *  was not obtained
	 * @throws IOException
	 */
	public UUID getSessionWriteLock(Session session)
		throws IOException;
	
	/**
	 * Get a write lock for a session.  Before writing a write lock
	 * must be obtained from the project.  
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return the session write lock or <code>null</code>
	 * @throws IOException
	 */
	public UUID getSessionWriteLock(String corpus, String session)
		throws IOException;
	
	/**
	 * Release the write lock for a session.
	 * 
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void releaseSessionWriteLock(Session session, UUID writeLock)
		throws IOException;
	
	/**
	 * Release the write lock for a session.
	 * 
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void releaseSessionWriteLock(String corpus, String session, UUID writeLock)
		throws IOException;
	
	/**
	 * Tells whether the given session is locked
	 * 
	 * @param session
	 * @return <code>true</code> if session is locked, <code>false</code>
	 *  otherwise
	 */
	public boolean isSessionLocked(Session session);
	
	/**
	 * Tells wheater the given session is locked
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return <code>true</code> if the session is locked, <code>false</code>
	 *  otherwise
	 */
	public boolean isSessionLocked(String corpus, String session);
	
	/**
	 * Save a session
	 * 
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void saveSession(Session session, UUID writeLock)
		throws IOException;
	
	/**
	 * Save a session to the specified corpus and new
	 * sessionName.
	 * 
	 * @param corpus
	 * @param sessionName
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void saveSession(String corpus, String sessionName, Session session, UUID writeLock)
		throws IOException;
	
	/**
	 * Remove a session from the project.  The writeLock 
	 * for the session is also released.
	 * 
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void removeSession(Session sesion, UUID writeLock)
		throws IOException;
	
	/**
	 * Remove a session from the project.  The writeLock 
	 * for the session is also released.
	 * 
	 * @parma corpus
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void removeSession(String corpus, String sesion, UUID writeLock)
		throws IOException;
	
	/**
	 * Returns the modification date for the given session
	 * 
	 * @param session
	 * 
	 * @return session modifiation date
	 */
	public LocalDateTime getSessionModificationTime(Session session);
	
	/**
	 * Returns the modification date for the specified session.
	 * 
	 * @param corpus
	 * @param session
	 */
	public LocalDateTime getSessionModificationTime(String corpus, String session);
	
	/**
	 * Returns the size on disk for the given session.
	 * 
	 * @param session
	 * 
	 * @return session size in bytes
	 */
	public long getSessionByteSize(Session session);
	
	/**
	 * Returns the size on disk for the given session.
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return session size in bytes
	 */
	public long getSessionByteSize(String corpus, String session);
	
	/**
	 * Get an input stream for the specified project resource.
	 * The resource name should be a relative path including filename.
	 * E.g., 'ca.phon.myplugin/module/corpus/session.dat'
	 * 
	 * @param resourceName
	 * 
	 * @return an input stream for the specified resource
	 * 
	 * @throws IOException
	 */
	public InputStream getResourceInputStream(String resourceName)
		throws IOException;
	
	/**
	 * Get an output stream for the specified resource.  If the resource
	 * does not exist it is created.  If the resource already exists
	 * it is overwritten.
	 * 
	 * @param resourceName
	 * 
	 * @return output stream for the specified resource
	 * 
	 * @throws IOException
	 */
	public OutputStream getResourceOutputStream(String resourceName)
		throws IOException;
}
