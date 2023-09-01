package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.*;

public class TranscriptScrollPane extends JScrollPane {
    private final TranscriptEditor transcriptEditor;
    private TranscriptRowHeader transcriptRowHeader;

    public TranscriptScrollPane(TranscriptEditor transcriptEditor) {
        super(transcriptEditor);
        this.transcriptEditor = transcriptEditor;
        initUI();
    }

    public TranscriptRowHeader getTranscriptRowHeader() {
        return transcriptRowHeader;
    }

    public void setTranscriptRowHeader(TranscriptRowHeader transcriptRowHeader) {
        this.transcriptRowHeader = transcriptRowHeader;
        setRowHeaderView(transcriptRowHeader);
    }

    private void initUI() {
    }
}
