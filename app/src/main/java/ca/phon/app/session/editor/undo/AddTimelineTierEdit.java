package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.Session;
import ca.phon.session.TimelineTier;

import javax.swing.undo.CannotUndoException;

/**
 * Undoable edit for adding a timeline tier to a session.
 *
 */
public class AddTimelineTierEdit extends SessionUndoableEdit {

    protected final TimelineTier timelineTier;

    public AddTimelineTierEdit(Session session, EditorEventManager editorEventManager, TimelineTier timelineTier) {
        super(session, editorEventManager);
        this.timelineTier = timelineTier;
    }

    @Override
    public String getPresentationName() {
        return "add timeline tier: " + timelineTier.getName();
    }

    @Override
    public void undo() throws CannotUndoException {
        final Session session = getSession();
        if (session.getTimeline().getTierNames().contains(timelineTier.getName())) {
            if(session.getTimeline().removeTier(timelineTier)) {
                // fire event
                final EditorEventManager editorEventManager = getEditorEventManager();
                if (editorEventManager != null) {
                    final EditorEvent<EditorEventType.TimelineTierRemoveData> event = new EditorEvent<>(EditorEventType.TimelineTierRemove, getSource(),
                            new EditorEventType.TimelineTierRemoveData(this.timelineTier.getName()));
                    editorEventManager.queueEvent(event);
                }
            }
        }
    }

    @Override
    public void doIt() {
        final Session session = getSession();
        if (session.getTimeline().getTierNames().contains(timelineTier.getName())
                || session.getTimeline().getRecordTimelineTiers().contains(timelineTier.getName())) {
            return;
        }
        if (session.getTimeline().addTier(timelineTier)) {
            // fire event
            final EditorEventManager editorEventManager = getEditorEventManager();
            if (editorEventManager != null) {
                final EditorEvent<EditorEventType.TimelineTierAddData> event = new EditorEvent<>(EditorEventType.TimelineTierAdd, getSource(),
                        new EditorEventType.TimelineTierAddData(this.timelineTier.getName()));
                editorEventManager.queueEvent(event);
            }
        }
    }


}
