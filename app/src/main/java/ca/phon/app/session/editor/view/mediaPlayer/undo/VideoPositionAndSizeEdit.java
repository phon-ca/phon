package ca.phon.app.session.editor.view.mediaPlayer.undo;

import ca.phon.app.session.editor.undo.SessionUndoableEdit;
import ca.phon.app.session.editor.view.mediaPlayer.MediaPlayerEditorView;

import javax.swing.undo.CannotUndoException;
import java.awt.*;

public class VideoPositionAndSizeEdit extends MediaPlayerViewEdit {

    private final int x;

    private final int prevX;

    private final int y;

    private final int prevY;

    private final int width;

    private final int prevWidth;

    private final int height;

    private final int prevHeight;

    public VideoPositionAndSizeEdit(MediaPlayerEditorView view, int x, int y, int width, int height,
                                    int prevX, int prevY, int prevWidth, int prevHeight) {
        super(view);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.prevX = prevX;
        this.prevY = prevY;
        this.prevWidth = prevWidth;
        this.prevHeight = prevHeight;
    }

    @Override
    public void doIt() {
        getView().setMediaCanvasPosition(new Point(x, y));
        getView().setMediaCanvasSize(new Dimension(width, height));
        getView().setupMediaCanvasBounds();
    }

    @Override
    public void undo() throws CannotUndoException {
        getView().setMediaCanvasPosition(new Point(prevX, prevY));
        getView().setMediaCanvasSize(new Dimension(prevWidth, prevHeight));
        getView().setupMediaCanvasBounds();
    }

    @Override
    public String getPresentationName() {
        return "Video position and size changed";
    }
}
