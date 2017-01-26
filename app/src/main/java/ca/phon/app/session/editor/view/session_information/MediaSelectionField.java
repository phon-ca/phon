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
package ca.phon.app.session.editor.view.session_information;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
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
	
	private final static Logger LOGGER = Logger.getLogger(MediaSelectionField.class.getName());
	
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
		PhonWorker.getInstance().invokeLater( () -> setupTextCompleter() );
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
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void setupTextCompleter() {
		final List<String> mediaIncludePaths = ( project != null ? 
				MediaLocator.getMediaIncludePaths(project) : MediaLocator.getMediaIncludePaths());
		for(String path:mediaIncludePaths) {
			final Path mediaFolder = Paths.get(path);
			if(!Files.exists(mediaFolder)) continue;
			
			scanPath(mediaFolder, true);
		}
		final TextCompleter completer = new TextCompleter(completerModel);
		// completion should not be in lower-case
		completer.setUseDataForCompletion(true);
		SwingUtilities.invokeLater( () -> completer.install(getTextField()) );
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
			
			for(String includePath:MediaLocator.getMediaIncludePaths(getProject())) {
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
		
		if(getTextField().getState() == FieldState.INPUT && txt.length() > 0) {
			File mediaLocatorFile = MediaLocator.findMediaFile(txt, project, null);
			if(mediaLocatorFile != null) {
				retVal = mediaLocatorFile;
			} else {
				retVal = new File(txt);
			}
		}
		
		return retVal;
	}
	
}
