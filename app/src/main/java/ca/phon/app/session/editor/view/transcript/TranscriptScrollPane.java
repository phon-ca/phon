package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * The {@link JScrollPane} that contains the {@link TranscriptEditor}
 * */
public class TranscriptScrollPane extends JScrollPane {
    /**
     * A reference to the {@link TranscriptEditor} the scroll pane contains
     * */
    private final TranscriptEditor transcriptEditor;
    /**
     * A reference to the gutter shown to the left of the editor
     * */
    private TranscriptScrollPaneGutter gutter;

    /**
     * Constructor
     *
     * @param transcriptEditor the transcript editor that will be contained in the scroll pane
     * */
    public TranscriptScrollPane(TranscriptEditor transcriptEditor) {
        super(transcriptEditor);
        this.transcriptEditor = transcriptEditor;
        registerEditorActions();
        initUI();
    }

    /**
     * Gets a reference to the gutter
     *
     * @return a reference to the gutter of this scroll pane
     * */
    public TranscriptScrollPaneGutter getGutter() {
        return gutter;
    }

    /**
     * Sets up the UI for the scroll pane
     * */
    private void initUI() {
//        this.gutter = new TranscriptScrollPaneGutter(transcriptEditor);
//        setRowHeaderView(gutter);

        transcriptEditor.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
//            gutter.setPreferredSize(new Dimension(
//                (int) gutter.getPreferredSize().getWidth(),
//                transcriptEditor.getHeight())
//            );
//            revalidate();
//            repaint();
            }
        });
    }

    /**
     * Registers an action for when the record is changed in single record view mode
     * */
    private void registerEditorActions() {
        transcriptEditor.getEventManager().registerActionForEvent(
            TranscriptEditor.recordChangedInSingleRecordMode,
            this::onRecordChanged,
            EditorEventManager.RunOn.AWTEventDispatchThread
        );
    }

    /**
     * Runs when the current record is changed in single record view mode.
     * Repaints the scroll pane and everything in it.
     *
     * @param editorEvent the event that changed the current record
     * */
    private void onRecordChanged(EditorEvent<Void> editorEvent) {
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }
}
