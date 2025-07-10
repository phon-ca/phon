package ca.phon.app.session.editor.view.mediaPlayer.undo;

import ca.phon.app.session.editor.view.mediaPlayer.MediaPlayerEditorView;

/**
 * Toggle media player between embedded and external modes.
 *
 */
public class EmbeddedExternalEdit extends MediaPlayerViewEdit {

    private final boolean embedded;

    public EmbeddedExternalEdit(MediaPlayerEditorView view, boolean embedded) {
        super(view);
        this.embedded = embedded;
    }

    @Override
    public void doIt() {
        if(embedded) {
            // close accessory window for the media player

        } else {

        }
    }

    @Override
    public void undo() {
    }

}
