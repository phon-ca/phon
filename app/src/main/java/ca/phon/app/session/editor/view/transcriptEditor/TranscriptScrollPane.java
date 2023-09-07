package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TranscriptScrollPane extends JScrollPane {
    private final TranscriptEditor transcriptEditor;
    private TranscriptRowHeader transcriptRowHeader;

    public TranscriptScrollPane(TranscriptEditor transcriptEditor) {
        super(transcriptEditor);
        this.transcriptEditor = transcriptEditor;
        registerEditorActions();
        initUI();
    }

    public TranscriptRowHeader getTranscriptRowHeader() {
        return transcriptRowHeader;
    }

    private void initUI() {
        this.transcriptRowHeader = new TranscriptRowHeader(transcriptEditor);
        setRowHeaderView(transcriptRowHeader);

        transcriptEditor.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                transcriptRowHeader.setPreferredSize(new Dimension(
                    (int)transcriptRowHeader.getPreferredSize().getWidth(),
                    transcriptEditor.getHeight())
                );
                revalidate();
                repaint();
            }
        });
    }

    private void registerEditorActions() {
        transcriptEditor.getEventManager().registerActionForEvent(
            TranscriptEditor.recordChangedInSingleRecordMode,
            this::onRecordChanged,
            EditorEventManager.RunOn.AWTEventDispatchThread
        );
    }

    private void onRecordChanged(EditorEvent<Void> editorEvent) {
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }
}
