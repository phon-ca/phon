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
package ca.phon.app.session.editor.view.session_information;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.text.DefaultTextCompleterModel;
import ca.phon.ui.text.FileSelectionField;
import ca.phon.ui.text.PromptedTextField.FieldState;
import ca.phon.ui.text.TextCompleter;
import ca.phon.worker.PhonWorker;

public class MediaSelectionField extends FileSelectionField {
	
	private final static Logger LOGGER = Logger.getLogger(MediaSelectionField.class.getName());
	
	private static final long serialVersionUID = 5171333221664140205L;
	
	private WeakReference<SessionEditor> editorRef;
	
	private Project project;
	
	private final DefaultTextCompleterModel completerModel = new DefaultTextCompleterModel();
	
	public MediaSelectionField() {
		super();
		setFileFilter(FileFilter.mediaFilter);
		setupTextCompleter();
	}

	public MediaSelectionField(Project project) {
		super();
		this.project = project;
		textField.setPrompt("Session media location");
		PhonWorker.getInstance().invokeLater( () -> {setupTextCompleter(); } );
	}
	
	protected void addTextCompletion(Path mediaFile) {
		final File file = mediaFile.toFile();
		if(getFileFilter() != null && !getFileFilter().accept(file)) return;
		try {
			if(Files.isHidden(mediaFile)) return;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return;
		}
		final String name = mediaFile.getFileName().toString();
		completerModel.addCompletion(name, "<html>" + name + " <i>(" + mediaFile.toString() + ")</i></html>");
	}
	
	private void setupTextCompleter() {
		final List<String> mediaIncludePaths = ( project != null ? 
				MediaLocator.getMediaIncludePaths(project) : MediaLocator.getMediaIncludePaths());
		for(String path:mediaIncludePaths) {
			final Path mediaFolder = Paths.get(path);
			if(!Files.exists(mediaFolder)) continue;
			try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(mediaFolder)) {
				dirStream.forEach( this::addTextCompletion );
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		final TextCompleter completer = new TextCompleter(completerModel);
		SwingUtilities.invokeLater( () -> { completer.install(getTextField()); } );
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
			final File parentFolder = f.getParentFile();
			if(parentFolder != null) {
				for(String includePath:MediaLocator.getMediaIncludePaths(project)) {
					if(includePath.equals(parentFolder.getAbsolutePath())) {
						txt = f.getName();
						break;
					}
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
		
//		final PathExpander pe = new PathExpander();
		
		if(getTextField().getState() == FieldState.INPUT && txt.length() > 0) {
			File mediaLocatorFile = MediaLocator.findMediaFile(txt, project, null);
			if(mediaLocatorFile != null) {
				retVal = mediaLocatorFile;
			} else {
				retVal = new File(txt);
//						new File(pe.expandPath(txt));
			}
		}
		
		return retVal;
	}
	
}
