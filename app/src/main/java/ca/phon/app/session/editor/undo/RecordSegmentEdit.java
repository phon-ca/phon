package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;

public class RecordSegmentEdit extends SessionEditorUndoableEdit {

    private final Record record;

    private final float segmentStart;

    private final float segmentEnd;

    private final float prevStart;

    private final float prevEnd;

    public RecordSegmentEdit(SessionEditor editor, Record record, MediaSegment segment) {
        super(editor);

        this.record = record;
        this.segmentStart = segment.getStartValue();
        this.segmentEnd = segment.getEndValue();
        this.prevStart = record.getSegment().getRecordSegment().getStartValue();
        this.prevEnd = record.getSegment().getRecordSegment().getEndValue();
    }

    @Override
    public void doIt() {
        final MediaSegment recordSegment = this.record.getSegment().getRecordSegment();
        recordSegment.setStartValue(this.segmentStart);
        recordSegment.setEndValue(this.segmentEnd);

        EditorEvent<EditorEventType.RecordSegmentChangedData> segmentChangedEvt =
                new EditorEvent<>(EditorEventType.RecordSegmentChanged, getEditor(), new EditorEventType.RecordSegmentChangedData(this.record, this.segmentStart, this.segmentEnd));
        getEditor().getEventManager().queueEvent(segmentChangedEvt);
    }

    @Override
    public void undo() {
        final MediaSegment recordSegment = this.record.getSegment().getRecordSegment();
        recordSegment.setStartValue(this.prevStart);
        recordSegment.setEndValue(this.prevEnd);

        EditorEvent<EditorEventType.RecordSegmentChangedData> segmentChangedEvt =
                new EditorEvent<>(EditorEventType.RecordSegmentChanged, getEditor(), new EditorEventType.RecordSegmentChangedData(this.record, this.prevStart, this.prevEnd));
        getEditor().getEventManager().queueEvent(segmentChangedEvt);
    }

}
