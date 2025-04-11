package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Session;

/**
 * Undoable edit for a {@link MediaSegment} in a {@link Record}
 */
public class RecordSegmentEdit extends TierEdit<MediaSegment> {

    public RecordSegmentEdit(SessionEditor editor, Record record, MediaSegment segment) {
        this(editor.getSession(), editor.getEventManager(), record, segment);
    }

    public RecordSegmentEdit(Session session, EditorEventManager editorEventManager, Record record, MediaSegment segment) {
        super(session, editorEventManager, record, record.getSegmentTier(), segment);
    }

}
