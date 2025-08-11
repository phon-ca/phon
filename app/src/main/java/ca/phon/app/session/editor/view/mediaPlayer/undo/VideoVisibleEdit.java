package ca.phon.app.session.editor.view.mediaPlayer.undo;

import ca.phon.app.session.editor.view.mediaPlayer.MediaPlayerEditorView;

import javax.swing.undo.CannotUndoException;

/**
 * Undoable edit for changing the visibility of a video in the media player view.
 */
public class VideoVisibleEdit extends MediaPlayerViewEdit {

    private final boolean visible;

    public VideoVisibleEdit(MediaPlayerEditorView view, boolean visible) {
        super(view);
        this.visible = visible;
    }

    @Override
    public String getPresentationName() {
        return "Video visibility changed";
    }

    @Override
    public String getUndoPresentationName() {
        return "Undo video visibility change";
    }

    @Override
    public String getRedoPresentationName() {
        return "Redo video visibility change";
    }

    @Override
    public void doIt() {
        if(getView().isEmbedded()) {
            if(visible) {
                getView().showVideoInWindowGlassPane();
            } else {
                getView().hideMediaPlayerCanvas();
            }
        } else {
            getView().getPlayer().setVideoVisible(visible);
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        if(getView().isEmbedded()) {
            if(visible) {
                getView().hideMediaPlayerCanvas();
            } else {
                getView().showVideoInWindowGlassPane();
            }
        } else {
            getView().getPlayer().setVideoVisible(!visible);
        }
    }

}
