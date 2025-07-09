package ca.phon.app.session.editor.view.mediaPlayer.undo;

import ca.phon.app.session.editor.undo.SessionUndoableEdit;
import ca.phon.app.session.editor.view.mediaPlayer.MediaPlayerEditorView;

/**
 * Abstract class for media player view edits.
 * This class serves as a base for all undoable edits related to the media player view.
 */
public abstract class MediaPlayerViewEdit extends SessionUndoableEdit {

    private MediaPlayerEditorView view;

    /**
     * Constructor
     *
     * @param mediaPlayerEditorView the media player editor view associated with this edit
     */
    public MediaPlayerViewEdit(MediaPlayerEditorView mediaPlayerEditorView) {
        super(mediaPlayerEditorView.getEditor().getSession(), mediaPlayerEditorView.getEditor().getEventManager());
        this.view = mediaPlayerEditorView;
    }

    /**
     * Get the media player editor view associated with this edit.
     *
     * @return the media player editor view
     */
    public MediaPlayerEditorView getView() {
        return this.view;
    }
}
