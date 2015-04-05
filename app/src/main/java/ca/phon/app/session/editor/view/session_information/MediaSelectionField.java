package ca.phon.app.session.editor.view.session_information;

import java.awt.dnd.DropTarget;
import java.io.File;
import java.util.List;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.common.MediaFileDropListener;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.text.DefaultTextCompleterModel;
import ca.phon.ui.text.FileSelectionField;
import ca.phon.ui.text.PromptedTextField.FieldState;
import ca.phon.ui.text.TextCompleter;

public class MediaSelectionField extends FileSelectionField {
	
	private static final long serialVersionUID = 5171333221664140205L;
	
	private SessionEditor editor;
	
	private Project project;
	
	public MediaSelectionField() {
		super();
		setFileFilter(FileFilter.mediaFilter);
		setupTextCompleter();
		
		
	}

	public MediaSelectionField(Project project) {
		super();
		this.project = project;
		textField.setPrompt("Session media location");
		setupTextCompleter();
		
		new DropTarget(this, new MediaFileDropListener());
	}
	
	private void setupTextCompleter() {
		final DefaultTextCompleterModel completerModel = new DefaultTextCompleterModel();
		
		final List<String> mediaIncludePaths = ( project != null ? 
				MediaLocator.getMediaIncludePaths(project) : MediaLocator.getMediaIncludePaths());
		for(String path:mediaIncludePaths) {
			final File mediaFolder = new File(path);
			for(File file:mediaFolder.listFiles()) {
				if(getFileFilter() != null && !getFileFilter().accept(mediaFolder)) continue;
				final String name = file.getName();
				completerModel.addCompletion(name, "<html>" + name + " <i>(" + file.getAbsolutePath() + ")</i></html>");
			}
		}
		final TextCompleter completer = new TextCompleter(completerModel);
		completer.install(super.getTextField());
	}

	public void setEditor(SessionEditor editor) {
		this.editor = editor;
	}
	
	public SessionEditor getEditor() {
		return this.editor;
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
