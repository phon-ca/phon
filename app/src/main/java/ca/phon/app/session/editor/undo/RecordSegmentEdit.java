package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;

public class RecordSegmentEdit extends SessionUndoableEdit {

    private final Record record;

    private MediaSegment segment;

    private MediaSegment prevSegment;

    private boolean valueAdjusting = true;

    public RecordSegmentEdit(SessionEditor editor, Record record, MediaSegment segment) {
        this(editor.getSession(), editor.getEventManager(), record, segment);
    }

    public RecordSegmentEdit(Session session, EditorEventManager editorEventManager, Record record, MediaSegment segment) {
        super(session, editorEventManager);

        this.record = record;
        this.segment = segment;
        this.prevSegment = record.getMediaSegment();
    }

    public boolean isValueAdjusting() {
        return valueAdjusting;
    }

    public void setValueAdjusting(boolean valueAdjusting) {
        this.valueAdjusting = valueAdjusting;
    }

    @Deprecated
    public boolean isFireHardChangeOnUndo() {
        return !isValueAdjusting();
    }

    @Deprecated
    public void setFireHardChangeOnUndo(boolean fireHardChangeOnUndo) {
        setValueAdjusting(!fireHardChangeOnUndo);
    }

    @Override
    public void doIt() {
        this.record.setMediaSegment(this.segment);
        fireChangeEvent(this.prevSegment, this.segment);
    }

    @Override
    public void undo() {
        this.record.setMediaSegment(this.prevSegment);
        fireChangeEvent(this.segment, this.prevSegment);
    }

    private void fireChangeEvent(MediaSegment prevSegment, MediaSegment segment) {
        final EditorEvent<EditorEventType.TierChangeData> segChangeEvt =
                new EditorEvent<>(EditorEventType.TierChange, getSource(),
                        new EditorEventType.TierChangeData(Transcriber.VALIDATOR, record, record.getSegmentTier(), prevSegment, segment, isValueAdjusting()));
        getEditorEventManager().queueEvent(segChangeEvt);
    }

}
