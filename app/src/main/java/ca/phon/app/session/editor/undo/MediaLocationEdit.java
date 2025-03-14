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
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.*;
import ca.phon.media.MediaLocator;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.util.PrefHelper;
import org.apache.commons.io.FilenameUtils;

import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.io.File;

public class MediaLocationEdit extends SessionUndoableEdit {

	/**
	 * User pref property for keeping extension when setting media location
	 */
	public final static String KEEP_MEDIA_EXTENSION_PROP = MediaLocationEdit.class.getName() + ".keepExtension";
	public final boolean DEFAULT_KEEP_MEDIA_EXTENSION = false;

	private final boolean keepExtension = PrefHelper.getBoolean(KEEP_MEDIA_EXTENSION_PROP, DEFAULT_KEEP_MEDIA_EXTENSION);

	private final String mediaLocation;
	
	private String oldLocation;

	private Project project;

	public MediaLocationEdit(SessionEditor editor, String mediaLocation) {
		this(editor.getProject(), editor.getSession(), editor.getEventManager(), mediaLocation);
	}

	public MediaLocationEdit(Project project, Session session, EditorEventManager editorEventManager, String mediaLocation) {
		super(session, editorEventManager);
		this.project = project;
		this.mediaLocation = mediaLocation;
	}
	
	public String getMediaLocation() {
		File mediaFile = new File(mediaLocation);
		if(mediaFile.isAbsolute()) {
			// check media include paths
			final String resolvedPath = MediaLocator.getRelativeMediaFilePath(project, mediaFile.getAbsolutePath());
			if(resolvedPath.equals(mediaFile.getAbsolutePath())) {
				// attempt to resolve relative to session file location in project
				final String projectLocation = project.getLocation();
				final String corpusPath = project.getCorpusPath(getSession().getCorpus());
				if(mediaLocation.startsWith(projectLocation)) {
					// relative path to corpusPath parent
					final File corpusFolder = new File(corpusPath);
					final File mediaFileParent = mediaFile.getParentFile();
					final File relativePath = corpusFolder.toPath().relativize(mediaFileParent.toPath()).toFile();
					mediaFile = new File(relativePath, mediaFile.getName());
					if(!keepExtension) {
						mediaFile = new File(FilenameUtils.removeExtension(mediaFile.toString()));
					}
				}
			} else {
				mediaFile = new File(resolvedPath);
				if(!keepExtension) {
					mediaFile = new File(FilenameUtils.removeExtension(mediaFile.toString()));
				}
			}
		}
		return mediaFile.toString();
	}
	
	public String getOldLocation() {
		return this.oldLocation;
	}

	@Override
	public void undo() throws CannotUndoException {
		final Session session = getSession();
		
		if(session.getMediaLocation() == null && getOldLocation() != null
				|| session.getMediaLocation() != null && getOldLocation() == null
				|| session.getMediaLocation() != null && !session.getMediaLocation().equals(getOldLocation())
				|| getOldLocation() != null && !getOldLocation().equals(session.getMediaLocation())) {
			session.setMediaLocation(getOldLocation());
			final EditorEvent<EditorEventType.SessionMediaChangedData> ee =
					new EditorEvent<>(EditorEventType.SessionMediaChanged, (Component) getSource(), new EditorEventType.SessionMediaChangedData(getMediaLocation(), getOldLocation()));
			getEditorEventManager().queueEvent(ee);
		}
	}

	@Override
	public void doIt() {
		final Session session = getSession();
		
		if(session == null) return;
		
		oldLocation = session.getMediaLocation();
		String mediaLocation = (getMediaLocation() != null && getMediaLocation().strip().length() > 0 ? getMediaLocation() : null);
		
		if(oldLocation == null && mediaLocation != null
				|| oldLocation != null && mediaLocation == null
				|| oldLocation != null && !oldLocation.equals(mediaLocation)) {
			session.setMediaLocation(mediaLocation);
			final EditorEvent<EditorEventType.SessionMediaChangedData> ee =
					new EditorEvent<>(EditorEventType.SessionMediaChanged, (Component) getSource(), new EditorEventType.SessionMediaChangedData(getOldLocation(), getMediaLocation()));
			getEditorEventManager().queueEvent(ee);
		}
	}

}
