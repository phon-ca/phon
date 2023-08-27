package ca.phon.app.session.editor.view.transcriptEditor;

import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;

public class TranscriptEditorScrollPane extends JScrollPane {
    private final TranscriptEditor transcriptEditor;
    private TranscriptEditorRowHeader transcriptEditorRowHeader;
    private TranscriptEditorStatusBar transcriptEditorStatusBar;

    public TranscriptEditorScrollPane(TranscriptEditor transcriptEditor) {
        super(transcriptEditor);
        this.transcriptEditor = transcriptEditor;
        initUI();
    }

    public TranscriptEditorRowHeader getTranscriptEditorRowHeader() {
        return transcriptEditorRowHeader;
    }

    public void setTranscriptEditorRowHeader(TranscriptEditorRowHeader transcriptEditorRowHeader) {
        this.transcriptEditorRowHeader = transcriptEditorRowHeader;
        setRowHeaderView(transcriptEditorRowHeader);
    }

    public TranscriptEditorStatusBar getTranscriptEditorStatusBar() {
        return transcriptEditorStatusBar;
    }

    public void setTranscriptEditorStatusBar(TranscriptEditorStatusBar transcriptEditorStatusBar) {
        this.transcriptEditorStatusBar = transcriptEditorStatusBar;
        setColumnHeaderView(transcriptEditorStatusBar);
    }

    private void initUI() {
    }
}
