package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.session.Session;

import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

public class TranscriptEditorKit extends StyledEditorKit {
    public static String CONTENT_TYPE = "phon/transcript";
    public Session session;
    private final TranscriptViewFactory viewFactory;

    public TranscriptEditorKit(Session session) {
        this.session = session;
        viewFactory = new TranscriptViewFactory();
    }

    @Override
    public Document createDefaultDocument() {
        TranscriptDocument doc = new TranscriptDocument();
        doc.setSession(session);
        return doc;
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
