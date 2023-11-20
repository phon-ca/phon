package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

/**
 * The {@link javax.swing.text.EditorKit} used by the {@link TranscriptEditor}
 * */
public class TranscriptEditorKit extends StyledEditorKit {
    public static String CONTENT_TYPE = "phon/transcript";
    private final TranscriptViewFactory viewFactory;

    public TranscriptEditorKit() {
        viewFactory = new TranscriptViewFactory();
    }

    @Override
    public Document createDefaultDocument() {
        return new TranscriptDocument();
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public ViewFactory getViewFactory() {
        return viewFactory;
    }
}
