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
            getView().setEmbedded(true);
            getView().getEditor().getViewModel().hideView(MediaPlayerEditorView.VIEW_NAME);
            getView().getEditor().getViewModel().showView(MediaPlayerEditorView.VIEW_NAME);
        } else {
            getView().moveToExternalWindow();
        }
    }

    @Override
    public void undo() {
        if(embedded) {
            getView().moveToExternalWindow();
        } else {
            getView().setEmbedded(true);
            getView().getEditor().getViewModel().hideView(MediaPlayerEditorView.VIEW_NAME);
            getView().getEditor().getViewModel().showView(MediaPlayerEditorView.VIEW_NAME);
        }
    }

}
