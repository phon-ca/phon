package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.*;

public class TranscriptEditorScrollPane extends JScrollPane {
    private final TranscriptEditor transcriptEditor;
    private TranscriptEditorRowHeader transcriptEditorRowHeader;

    public TranscriptEditorScrollPane(TranscriptEditor transcriptEditor) {
        super(transcriptEditor);
        this.transcriptEditor = transcriptEditor;
    }

    public TranscriptEditorRowHeader getTranscriptEditorRowHeader() {
        return transcriptEditorRowHeader;
    }

    public void setTranscriptEditorRowHeader(TranscriptEditorRowHeader transcriptEditorRowHeader) {
        this.transcriptEditorRowHeader = transcriptEditorRowHeader;
    }
}
