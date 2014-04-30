package ca.phon.app.session.editor.view.media_player.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;

/**
 * Base class for media layer actions.
 */
public abstract class MediaPlayerAction extends SessionEditorAction {

	private static final long serialVersionUID = 2965979837203474011L;

	private final MediaPlayerEditorView editorView;
	
	public MediaPlayerAction(SessionEditor editor, MediaPlayerEditorView view) {
		super(editor);
		this.editorView = view;
	}
	
	public MediaPlayerEditorView getMediaPlayerView() {
		return this.editorView;
	}
	
}
