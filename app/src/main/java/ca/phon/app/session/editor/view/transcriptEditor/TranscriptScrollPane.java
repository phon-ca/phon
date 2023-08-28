package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.*;

public class TranscriptScrollPane extends JScrollPane {
    private final TranscriptEditor transcriptEditor;
    private TranscriptRowHeader transcriptRowHeader;
    private TranscriptStatusBar transcriptStatusBar;

    public TranscriptScrollPane(TranscriptEditor transcriptEditor) {
        super(transcriptEditor);
        this.transcriptEditor = transcriptEditor;
        initUI();
    }

    public TranscriptRowHeader getTranscriptEditorRowHeader() {
        return transcriptRowHeader;
    }

    public void setTranscriptEditorRowHeader(TranscriptRowHeader transcriptRowHeader) {
        this.transcriptRowHeader = transcriptRowHeader;
        setRowHeaderView(transcriptRowHeader);
    }

    public TranscriptStatusBar getTranscriptEditorStatusBar() {
        return transcriptStatusBar;
    }

    public void setTranscriptEditorStatusBar(TranscriptStatusBar transcriptStatusBar) {
        this.transcriptStatusBar = transcriptStatusBar;
        setColumnHeaderView(transcriptStatusBar);
    }

    private void initUI() {
    }
}
