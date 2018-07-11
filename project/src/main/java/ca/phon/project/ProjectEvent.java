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
package ca.phon.project;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for project events.
 *
 *
 */
public class ProjectEvent {

	/**
	 * Event type
	 */
	public static enum ProjectEventType {
		CORPUS_ADDED,
		CORPUS_REMOVED,
		PROJECT_MEDIAFOLDER_CHANGED,
		CORPUS_DESCRIPTION_CHANGED,
		CORPUS_MEDIAFOLDER_CHANGED,
		SESSION_ADDED,
		SESSION_REMOVED,
		SESSION_CHANGED,
		PROJECT_NAME_CHANGED,
		PROJECT_UUID_CHANGED;
	};

	/**
	 * Event properties
	 */
	public static enum ProjectEventProp {
		CORPUS,
		SESSION,
		OLD_CORPUS_DESCRIPTION,
		NEW_CORPUS_DESCRIPTION,
		OLD_MEDIAFOLDER,
		NEW_MEDIAFOLDER,
		OLD_PROJECT_NAME,
		NEW_PROJECT_NAME,
		OLD_PROJECT_UUID,
		NEW_PROJECT_UUID;
	};

	/**
	 * Event type
	 */
	private ProjectEventType eventType;

	/**
	 * Properties
	 *
	 */
	private final Map<ProjectEventProp, String> eventProps =
			new HashMap<ProjectEvent.ProjectEventProp, String>();

	/*
	 * Static initializers
	 */

	/**
	 * Create a new corpus added event.
	 *
	 * @param corpus the name of the added corpus
	 *
	 * @return
	 */
	public static ProjectEvent newCorpusAddedEvent(String corpus) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.CORPUS_ADDED);
		retVal.setProperty(ProjectEventProp.CORPUS, corpus);
		return retVal;
	}

	/**
	 * Create a new corpus removed event
	 *
	 * @param corpus the name of the removed corpus
	 *
	 * @return
	 */
	public static ProjectEvent newCorpusRemovedEvent(String corpus) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.CORPUS_REMOVED);
		retVal.setProperty(ProjectEventProp.CORPUS, corpus);
		return retVal;
	}

	/**
	 * Create a new corpus description changed event.
	 *
	 * @param corpus
	 * @param olddescription
	 * @param newDescription
	 *
	 * @return
	 */
	public static ProjectEvent newCorpusDescriptionChangedEvent(String corpus, String oldDescription, String newDescription) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.CORPUS_DESCRIPTION_CHANGED);
		retVal.setProperty(ProjectEventProp.CORPUS, corpus);
		retVal.setProperty(ProjectEventProp.OLD_CORPUS_DESCRIPTION, oldDescription);
		retVal.setProperty(ProjectEventProp.NEW_CORPUS_DESCRIPTION, newDescription);
		return retVal;
	}

	public static ProjectEvent newCorpusMediaFolderChangedEvent(String corpus, String oldFolder, String newFolder) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.CORPUS_MEDIAFOLDER_CHANGED);
		retVal.setProperty(ProjectEventProp.CORPUS, corpus);
		retVal.setProperty(ProjectEventProp.OLD_MEDIAFOLDER, oldFolder);
		retVal.setProperty(ProjectEventProp.NEW_MEDIAFOLDER, newFolder);
		return retVal;
	}

	public static ProjectEvent newProjectMediaFolderChangedEVent(String oldFolder, String newFolder) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.PROJECT_MEDIAFOLDER_CHANGED);
		retVal.setProperty(ProjectEventProp.OLD_MEDIAFOLDER, oldFolder);
		retVal.setProperty(ProjectEventProp.NEW_MEDIAFOLDER, newFolder);
		return retVal;
	}

	/**
	 * Create a new session added event
	 *
	 * @param corpus
	 * @param session
	 *
	 * @return
	 */
	public static ProjectEvent newSessionAddedEvent(String corpus, String session) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.SESSION_ADDED);
		retVal.setProperty(ProjectEventProp.CORPUS, corpus);
		retVal.setProperty(ProjectEventProp.SESSION, session);
		return retVal;
	}

	/**
	 * Create a new session removed event.
	 *
	 * @param corpus
	 * @param session
	 *
	 * @return
	 */
	public static ProjectEvent newSessionRemovedEvent(String corpus, String session) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.SESSION_REMOVED);
		retVal.setProperty(ProjectEventProp.CORPUS, corpus);
		retVal.setProperty(ProjectEventProp.SESSION, session);
		return retVal;
	}

	/**
	 * Create a new session chagned event.
	 *
	 * @param corpus
	 * @param session
	 *
	 * @return
	 */
	public static ProjectEvent newSessionChagnedEvent(String corpus, String session) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.SESSION_CHANGED);
		retVal.setProperty(ProjectEventProp.CORPUS, corpus);
		retVal.setProperty(ProjectEventProp.SESSION, session);
		return retVal;
	}

	/**
	 * Create a new project name changed event
	 *
	 * @param oldName
	 * @param newName
	 *
	 * @return
	 */
	public static ProjectEvent newNameChangedEvent(String oldName, String newName) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.PROJECT_NAME_CHANGED);
		retVal.setProperty(ProjectEventProp.OLD_PROJECT_NAME, oldName);
		retVal.setProperty(ProjectEventProp.NEW_PROJECT_NAME, newName);
		return retVal;
	}

	/**
	 * Create a new project uuid chagned event
	 *
	 * @param oldUUID
	 * @param newUUID
	 *
	 * @return
	 */
	public static ProjectEvent newUUIDChangedEvent(String oldUUID, String newUUID) {
		final ProjectEvent retVal = new ProjectEvent(ProjectEventType.PROJECT_UUID_CHANGED);
		retVal.setProperty(ProjectEventProp.OLD_PROJECT_UUID, oldUUID);
		retVal.setProperty(ProjectEventProp.NEW_PROJECT_UUID, newUUID);
		return retVal;
	}

	/**
	 * Hidden constructor
	 */
	private ProjectEvent(ProjectEventType eventType) {
		super();
		this.eventType = eventType;
	}

	/**
	 * Get the event type
	 *
	 * @return the project event type
	 */
	public ProjectEventType getEventType() {
		return this.eventType;
	}

	/**
	 * Set the event type
	 */
	public void setEventType(ProjectEventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * Get the value of the specified property.
	 *
	 * @param prop
	 * @return value
	 */
	public String getProperty(ProjectEventProp prop) {
		return eventProps.get(prop);
	}

	/**
	 * Set the value of an event property
	 *
	 * @param prop
	 * @param value
	 */
	public void setProperty(ProjectEventProp prop, String value) {
		eventProps.put(prop, value);
	}

}
