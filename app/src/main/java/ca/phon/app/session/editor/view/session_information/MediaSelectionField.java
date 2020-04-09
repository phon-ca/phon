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
package ca.phon.app.session.editor.view.session_information;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.text.DefaultTextCompleterModel;
import ca.phon.ui.text.FileSelectionField;
import ca.phon.ui.text.PromptedTextField.FieldState;
import ca.phon.ui.text.TextCompleter;
import ca.phon.worker.PhonWorker;

/**
 * Media selection field for {@link SessionInfoEditorView}.
 * This field adds a text completer for media files found in the
 * list of media folders setup in application preferences.  Files
 * found in one of these folders are displayed with a relative path.
 *
 */
public class MediaSelectionField extends FileSelectionField {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(MediaSelectionField.class.getName());

	private static final long serialVersionUID = 5171333221664140205L;

	private WeakReference<SessionEditor> editorRef;

	private Project project;

	private final DefaultTextCompleterModel completerModel = new DefaultTextCompleterModel();

	public MediaSelectionField() {
		this(null);
	}

	public MediaSelectionField(Project project) {
		super();
		this.project = project;
		textField.setPrompt("Session media location");
		setFileFilter(FileFilter.mediaFilter);
		
		setupInputMap();
		
		PhonWorker.getInstance().invokeLater( () -> setupTextCompleter() );
	}
	
	private void setupInputMap() {
		InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = getActionMap();
		
		KeyStroke saveKs = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		String saveId = "save";
		
		PhonUIAction validateAndSaveSessionAct = new PhonUIAction(this, "validateAndSaveSession");
		actionMap.put(saveId, validateAndSaveSessionAct);
		inputMap.put(saveKs, saveId);
		
		setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
		setActionMap(actionMap);
	}

	public void validateAndSaveSession(PhonActionEvent pae) {
		final File f = getSelectedFile();
		setFile(f);
		
		(new SaveSessionAction(getEditor())).actionPerformed(pae.getActionEvent());
	}
	
	/**
	 *
	 * @param rootPath
	 * @param mediaFile
	 */
	protected void addTextCompletion(Path path) {
		final String name = path.normalize().toString();
		completerModel.addCompletion(path.getFileName().toString(), name);
		completerModel.addCompletion(name, name);
	}

	private void scanPath(Path mediaPath, boolean recursive) {
		scanPath(mediaPath, mediaPath, true);
	}

	private void scanPath(Path rootPath, Path path, boolean recursive) {
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
			final Iterator<Path> childItr = dirStream.iterator();
			while(childItr.hasNext()) {
				final Path child = childItr.next();

				if(Files.isHidden(child)) continue;
				final File file = child.toFile();
				if(getFileFilter() != null && !getFileFilter().accept(file)) continue;

				if(Files.isDirectory(child) && recursive) {
					scanPath(rootPath, child, recursive);
				} else {
					final Path pathToAdd = rootPath.relativize(child);
					addTextCompletion(pathToAdd);
				}
			}
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

	private void setupTextCompleter() {
		final List<String> mediaIncludePaths = ( project != null ?
				MediaLocator.getMediaIncludePaths(project, getEditor().getSession().getCorpus()) : MediaLocator.getMediaIncludePaths());
		for(String path:mediaIncludePaths) {
			final Path mediaFolder = Paths.get(path);
			if(!Files.exists(mediaFolder)) continue;

			scanPath(mediaFolder, true);
		}
		SwingUtilities.invokeLater( () -> {
			final TextCompleter completer = new TextCompleter(completerModel);
			// completion should not be in lower-case
			completer.setUseDataForCompletion(true);
			completer.install(getTextField());
		});
	}

	public void setEditor(SessionEditor editor) {
		this.editorRef = new WeakReference<>(editor);
	}

	public SessionEditor getEditor() {
		return this.editorRef.get();
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return this.project;
	}

	@Override
	public void setFile(File f) {
		if(f == null) {
			textField.setText("");
		} else {
			textField.setState(FieldState.INPUT);

			String txt = f.getPath();

			for(String includePath:MediaLocator.getMediaIncludePaths(getProject(), getEditor().getSession().getCorpus())) {
				final Path path = Paths.get(includePath);
				Path mediaPath = f.toPath();

				if(mediaPath.startsWith(path)) {
					mediaPath = path.relativize(mediaPath).normalize();
					final String relativePath = mediaPath.toString();
					txt = relativePath;
					break;
				}
			}

			textField.setText(txt);
		}
		super.firePropertyChange(FILE_PROP, lastSelectedFile, f);
		lastSelectedFile = f;
	}

	@Override
	public void onBrowse() {
		if(getEditor() != null) {
			final MediaPlayerEditorView mediaPlayerView =
					(MediaPlayerEditorView)getEditor().getViewModel()
					.getView(MediaPlayerEditorView.VIEW_TITLE);
			if(mediaPlayerView != null) {
				if(mediaPlayerView.getPlayer() != null && mediaPlayerView.getPlayer().isPlaying())
					mediaPlayerView.getPlayer().pause();
			}
		}
		super.onBrowse();
	}

	@Override
	public File getSelectedFile() {
		final String txt = super.getText();
		File retVal = null;

		if(getTextField().getState() == FieldState.INPUT && txt != null && txt.length() > 0
				&& getEditor().getSession() != null) {
			File mediaLocatorFile = MediaLocator.findMediaFile(txt, project, getEditor().getSession().getCorpus());
			if(mediaLocatorFile != null) {
				retVal = mediaLocatorFile;
			} else {
				retVal = new File(txt);
			}
		}

		return retVal;
	}

}
