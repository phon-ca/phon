package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.GroupSegment;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;

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
        for(int gidx = 0; gidx < this.record.numberOfGroups(); gidx++) {
            final GroupSegment gseg = this.record.getGroupSegment().getGroup(gidx);
            this.record.getGroupSegment().setGroup(gidx, gseg);
        }
    }

    @Override
    public void undo() {
        this.record.setMediaSegment(this.prevSegment);
        for(int gidx = 0; gidx < this.record.numberOfGroups(); gidx++) {
            final GroupSegment gseg = this.record.getGroupSegment().getGroup(gidx);
            this.record.getGroupSegment().setGroup(gidx, gseg);
        }
    }

}
