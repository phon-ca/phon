package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.action.PhonUIAction;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.undo.UndoManager;

public class TranscriptionSelectorComponentFactory implements ComponentFactory {
    private final Session session;
    private final EditorEventManager eventManager;
    private final SessionEditUndoSupport undoSupport;
    private final Record record;
    private final Tier<?> tier;
    private final String transcriptionText;

    public TranscriptionSelectorComponentFactory(
        Session session,
        EditorEventManager eventManager,
        SessionEditUndoSupport undoSupport,
        Record record,
        Tier<?> tier,
        String transcriptionText
    ) {
        this.session = session;
        this.eventManager = eventManager;
        this.undoSupport = undoSupport;
        this.record = record;
        this.tier = tier;
        this.transcriptionText = transcriptionText;
    }

    @Override
    public JComponent createComponent(AttributeSet attrs) {

        JButton selectTranscriptionButton = new JButton();
        PhonUIAction selectTranscriptionAction = PhonUIAction.runnable(this::selectTranscription);
        selectTranscriptionAction.putValue(PhonUIAction.NAME, "Select");

        selectTranscriptionButton.setAction(selectTranscriptionAction);
        return selectTranscriptionButton;
    }

    private void selectTranscription() {
        System.out.println("Transcription selected");

        Tier<?> dummy = SessionFactory.newFactory().createTier("dummy", tier.getDeclaredType());
        dummy.setText(transcriptionText);

        SwingUtilities.invokeLater(() -> {
            TierEdit<?> edit = new TierEdit(session, eventManager, Transcriber.VALIDATOR, record, tier, dummy.getValue());
            edit.setValueAdjusting(false);
            undoSupport.postEdit(edit);
            System.out.println("Tier data changed event posted");
        });
    }
}
