package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;

public class RecordSegmentEdit extends SessionEditorUndoableEdit {

    private final Record record;

    private final float segmentStart;

    private final float segmentEnd;

    private final float prevStart;

    private final float prevEnd;

    private boolean fireHardChangeOnUndo = false;

    public RecordSegmentEdit(SessionEditor editor, Record record, MediaSegment segment) {
        super(editor);

        this.record = record;
        this.segmentStart = segment.getStartValue();
        this.segmentEnd = segment.getEndValue();
        this.prevStart = record.getMediaSegment().getStartValue();
        this.prevEnd = record.getMediaSegment().getEndValue();
    }

    public boolean isFireHardChangeOnUndo() {
        return fireHardChangeOnUndo;
    }

    public void setFireHardChangeOnUndo(boolean fireHardChangeOnUndo) {
        this.fireHardChangeOnUndo = fireHardChangeOnUndo;
    }

    @Override
    public void doIt() {
        final MediaSegment oldSegment = SessionFactory.newFactory().createMediaSegment();
        final MediaSegment recordSegment = this.record.getMediaSegment();
        oldSegment.setSegment(recordSegment);
        recordSegment.setStartValue(this.segmentStart);
        recordSegment.setEndValue(this.segmentEnd);
        final EditorEvent<EditorEventType.TierChangeData> segmentChangedEvt =
                new EditorEvent<>(isFireHardChangeOnUndo() ? EditorEventType.TierChanged : EditorEventType.TierChange, getEditor(),
                        new EditorEventType.TierChangeData(this.record.getSegment(), 0, oldSegment, recordSegment));
        getEditor().getEventManager().queueEvent(segmentChangedEvt);
    }

    @Override
    public void undo() {
        final MediaSegment oldSegment = SessionFactory.newFactory().createMediaSegment();
        final MediaSegment recordSegment = this.record.getMediaSegment();
        oldSegment.setSegment(recordSegment);
        recordSegment.setStartValue(this.prevStart);
        recordSegment.setEndValue(this.prevEnd);
        final EditorEvent<EditorEventType.TierChangeData> segmentChangedEvt =
                new EditorEvent<>(isFireHardChangeOnUndo() ? EditorEventType.TierChanged : EditorEventType.TierChange, getEditor(),
                        new EditorEventType.TierChangeData(this.record.getSegment(), 0, oldSegment, recordSegment));
        getEditor().getEventManager().queueEvent(segmentChangedEvt);
    }

}
