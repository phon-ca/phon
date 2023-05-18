package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.GroupSegment;
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
        updateGroupSegments();
    }

    @Override
    public void undo() {
        this.record.setMediaSegment(this.prevSegment);
        fireChangeEvent(this.segment, this.prevSegment);
        updateGroupSegments();
    }

    private void fireChangeEvent(MediaSegment prevSegment, MediaSegment segment) {
        final EditorEvent<EditorEventType.TierChangeData> segChangeEvt =
                new EditorEvent<>(isFireHardChangeOnUndo() ? EditorEventType.TierChanged : EditorEventType.TierChange, getEditor(),
                        new EditorEventType.TierChangeData(record.getSegmentTier(), 0, prevSegment, segment));
        getEditor().getEventManager().queueEvent(segChangeEvt);
    }

    private void updateGroupSegments() {
        for(int gidx = 0; gidx < this.record.numberOfGroups(); gidx++) {
            final GroupSegment prevSeg = this.record.getGroupSegment().getGroup(gidx);
            final GroupSegment gseg = new GroupSegment(record, prevSeg.getStart(), prevSeg.getEnd());
            this.record.getGroupSegment().setGroup(gidx, gseg);

            final EditorEvent<EditorEventType.TierChangeData> gsegChangeEvt =
                    new EditorEvent<>(isFireHardChangeOnUndo() ? EditorEventType.TierChanged : EditorEventType.TierChange, getEditor(),
                            new EditorEventType.TierChangeData(record.getGroupSegment(), gidx, prevSeg, gseg));
            getEditor().getEventManager().queueEvent(gsegChangeEvt);
        }
    }

}
