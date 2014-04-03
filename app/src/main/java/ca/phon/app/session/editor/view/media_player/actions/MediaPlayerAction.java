package ca.phon.app.session.editor.view.media_player.actions;

import javax.swing.AbstractAction;

import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;

/**
 * Base class for media layer actions.
 */
public abstract class MediaPlayerAction extends AbstractAction {

	private static final long serialVersionUID = 2965979837203474011L;

	private final MediaPlayerEditorView editorView;
	
	public MediaPlayerAction(MediaPlayerEditorView view) {
		super();
		this.editorView = view;
	}
	
	public MediaPlayerEditorView getMediaPlayerView() {
		return this.editorView;
	}
	
}
