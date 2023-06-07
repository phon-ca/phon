package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;

public class RecordSegmentEdit extends SessionEditorUndoableEdit {

    private final Record record;

    private MediaSegment segment;

    private MediaSegment prevSegment;

    private boolean fireHardChangeOnUndo = false;

    public RecordSegmentEdit(SessionEditor editor, Record record, MediaSegment segment) {
        super(editor);

        this.record = record;
        this.segment = segment;
        this.prevSegment = record.getMediaSegment();
    }

    public boolean isFireHardChangeOnUndo() {
        return fireHardChangeOnUndo;
    }

    public void setFireHardChangeOnUndo(boolean fireHardChangeOnUndo) {
        this.fireHardChangeOnUndo = fireHardChangeOnUndo;
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
                new EditorEvent<>(isFireHardChangeOnUndo() ? EditorEventType.TierChanged : EditorEventType.TierChange, getEditor(),
                        new EditorEventType.TierChangeData(record.getSegmentTier(), 0, prevSegment, segment));
        getEditor().getEventManager().queueEvent(segChangeEvt);
    }

}
