/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.project;

import ca.phon.extensions.IExtendable;
import ca.phon.session.*;
import ca.phon.session.io.*;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Minimal interface for a phon Project.
 * Projects are responsible for providing access to corpora and sessions.
 * Implementations are responsible for providing the actual organization
 * of the project data.
 *
 * Additional project functionality is provided by the {@link IExtendable}
 * interface.  An example is the {@link ProjectResources} interface which
 * provides a way to listen access project resources.  To use this
 * functionality, do the following:
 *
 * <pre>
 * Project project = ...;
 * ProjectResources projectResources = project.getExtension(ProjectResources.class);
 * if(projectResources != null) {
 *     // do something with project resources
 * }
 * </pre>
 *
 * All extensions are optional and may not be available for all project types.
 */
public interface Project extends IExtendable {

	/**
	 * Project version
	 *
	 * @return the project version or 'unk' if not known
	 */
	public String getVersion();

	/**
	 * The location of the project.  Meaning is dependent on implementation.
	 * For {@link LocalProject}s this is the path to the project on disk.
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
	 * Get an iterator over the corpora in this project.
	 *
	 * @return iterator over corpora
	 */
	public Iterator<String> getCorpusIterator();

	/**
	 * Add corpus folder with given name
	 *
	 * @param name
	 * @throws IOException
	 */
	public void addCorpus(String name) throws IOException;

	/**
	 * Add a new corpus with the specified name.
	 *
	 * @param name
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
	 * Tests to see if a given corpus exists in the project.  This may be faster
	 * than calling {@link #getCorpusIterator()} and checking iteratively.
	 *
	 * @param corpus
	 *
	 * @return <code>true</code> if the corpus exists, <code>false</code>
	 */
	public boolean hasCorpus(String corpus);

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
	 * Has a custom project media folder been assigned
	 * 
	 * @return <code>true</code> if project folder has been customized
	 *
	 */
	public boolean hasCustomProjectMediaFolder();

	/**
	 * Get all media folders for the project
	 *
	 * @return list of media folders
	 */
	public List<String> getProjectMediaFolders();

	/**
	 * Add a media folder to the project
	 *
	 * @param mediaFolder
	 */
	public void addProjectMediaFolder(String mediaFolder);

	/**
	 * Add a media folder to the project at the specified index
	 *
	 * @param index
	 * @param mediaFolder
	 */
	public void addProjectMediaFolder(int index, String mediaFolder);

	/**
	 * Remove a media folder from the project
	 *
	 * @param mediaFolder
	 */
	public void removeProjectMediaFolder(String mediaFolder);

	/**
	 * Get an iterator over the sessions in the specified corpus.
	 *
	 * @param corpus
	 *
	 * @return iterator over sessions in the corpus
	 */
	public Iterator<String> getSessionIterator(String corpus);

	/**
	 * Test if a given session exists in the specified corpus.  This method is usually
	 * faster than calling {@link #getSessionIterator(String)} and checking
	 * iteratively.
	 *
	 * @param corpus the corpus name
	 * @param session the session name
	 *
	 * @return <code>true</code> if the session exists, <code>false</code> otherwise
	 */
	public boolean hasSession(String corpus, String session);

	/**
	 * Return the path to the given corpus.
	 *
	 * @param corpus
	 */
	public String getCorpusPath(String corpus);

	/**
	 * Set path of corpus.
	 *
	 * @param corpus
	 * @param path
	 */
	public void setCorpusPath(String corpus, String path);

	/**
	 * Return a set of participants which are found in the
	 * given collection of Sessions.  The participant objects
	 * returned by this method will include the {@link ParticipantHistory}
	 * extension.
	 *
	 * Participants from two sessions are considered to be the same
	 * if their ids, names and roles match.  If the speaker for some records
	 * is unidenified, a clone of Participant.UNKOWN will be added in the returned
	 * set.
	 *
	 * @param sessions
	 * @return a set of participants
	 */
	public Set<Participant> getParticipants(Collection<SessionPath> sessions);

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
	 * Open specified session using the provided reader.
	 *
	 * @param corpus
	 * @param session
	 * @param reader
	 *
	 * @return the session
	 *
	 * @throws IOException
	 */
	public Session openSession(String corpus, String session, SessionReader reader)
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
	 * Save a session writing the file using the given writer.
	 *
	 * @param corpus
	 * @param sessionName
	 * @param session
	 * @param writer
	 * @param writeLock
	 *
	 * @throws IOException
	 */
	public void saveSession(String corpus, String sessionName, Session session, SessionWriter writer, UUID writeLock)
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
	public void removeSession(Session session, UUID writeLock)
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
	public void removeSession(String corpus, String session, UUID writeLock)
		throws IOException;

}
