package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TranscriptScrollPane extends JScrollPane {
    private final TranscriptEditor transcriptEditor;
    private TranscriptScrollPaneGutter gutter;

    public TranscriptScrollPane(TranscriptEditor transcriptEditor) {
        super(transcriptEditor);
        this.transcriptEditor = transcriptEditor;
        registerEditorActions();
        initUI();
    }

    public TranscriptScrollPaneGutter getGutter() {
        return gutter;
    }

    private void initUI() {
        this.gutter = new TranscriptScrollPaneGutter(transcriptEditor);
        setRowHeaderView(gutter);

        transcriptEditor.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gutter.setPreferredSize(new Dimension(
                    (int) gutter.getPreferredSize().getWidth(),
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
