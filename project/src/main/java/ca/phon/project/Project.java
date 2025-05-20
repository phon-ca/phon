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
	String getVersion();

	/**
	 * The location of the project.  Meaning is dependent on implementation.
	 * For {@link LocalProject}s this is the path to the project on disk.
	 *
	 * @return the project location
	 */
	String getLocation();

	/**
	 * The name of the project.
	 *
	 * @return project name
	 */
	String getName();

	/**
	 * Project UUID
	 *
	 * UUID for the project
	 *
	 * @return uuid
	 */
	UUID getUUID();

	/**
	 * Get an iterator over the corpora in this project.
	 *
	 * @return iterator over corpora
	 */
	Iterator<String> getCorpusIterator();

	/**
	 * Tests to see if a given corpus exists in the project.  This may be faster
	 * than calling {@link #getCorpusIterator()} and checking iteratively.
	 *
	 * @param corpus
	 *
	 * @return <code>true</code> if the corpus exists, <code>false</code>
	 */
	boolean hasCorpus(String corpus);

	/**
	 * Get the description of the specified corpus.
	 *
	 * @param corpus the corpus name
	 */
	String getCorpusDescription(String corpus);

	/**
	 * Get an iterator over the sessions in the specified corpus.
	 *
	 * @param corpus
	 *
	 * @return iterator over sessions in the corpus
	 */
	Iterator<String> getSessionIterator(String corpus);

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
	boolean hasSession(String corpus, String session);

	/**
	 * Return the path to the given corpus.
	 *
	 * @param corpus
	 */
	String getCorpusPath(String corpus);

	/**
	 * Set path of corpus.
	 *
	 * @param corpus
	 * @param path
	 */
	void setCorpusPath(String corpus, String path);

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
	Set<Participant> getParticipants(Collection<SessionPath> sessions);

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
	Session openSession(String corpus, String session)
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
	Session openSession(String corpus, String session, SessionReader reader)
		throws IOException;

	/**
	 * Get path to the given session.
	 *
	 * @param session
	 *
	 * @return path to given session
	 */
	String getSessionPath(Session session);

	/**
	 * Get path to the given session.
	 *
	 * @param corpus
	 * @param session
	 *
	 * @return path to given session
	 */
	String getSessionPath(String corpus, String session);

}
