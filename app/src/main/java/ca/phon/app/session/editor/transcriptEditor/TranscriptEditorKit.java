package ca.phon.app.session.editor.transcriptEditor;

import ca.phon.csv.CSVReader;
import ca.phon.session.Session;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;
import java.io.IOException;
import java.io.InputStream;

public class TranscriptEditorKit extends StyledEditorKit {
    public static String CONTENT_TYPE = "phon/transcript";
    public Session session;
    private final TranscriptDocument doc;
    private final TranscriptViewFactory viewFactory;

    public TranscriptEditorKit(Session session) {
        this.session = session;
        doc = new TranscriptDocument(session);
        System.out.println(doc.getLabelMaxWidth());
        viewFactory = new TranscriptViewFactory(doc.getLabelMaxWidth());
    }

    @Override
    public Document createDefaultDocument() {
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

    public void debugInfo() {
        var counterMap = viewFactory.getCounterMap();
        for (var key : counterMap.keySet()) {
            System.out.println(key.toUpperCase() + ": " + counterMap.get(key));
        }
    }
}
