package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;

/**
 * Undoable edit for adding a timeline tier to a session.
 *
 */
public class AddTimelineTierEdit extends SessionUndoableEdit {

    protected final IntervalTier intervalTier;

    public AddTimelineTierEdit(Session session, EditorEventManager editorEventManager, IntervalTier intervalTier) {
        super(session, editorEventManager);
        this.intervalTier = intervalTier;
    }

    @Override
    public String getPresentationName() {
        return "add timeline tier: " + intervalTier.getName();
    }

    @Override
    public void undo() throws CannotUndoException {
        final Session session = getSession();
        if (session.getTimeline().getTierNames().contains(intervalTier.getName())) {
            if(session.getTimeline().removeTier(intervalTier)) {
                // fire event
                final EditorEventManager editorEventManager = getEditorEventManager();
                if (editorEventManager != null) {
                    final EditorEvent<EditorEventType.TimelineTierRemoveData> event = new EditorEvent<>(EditorEventType.TimelineTierRemove, getSource(),
                            new EditorEventType.TimelineTierRemoveData(this.intervalTier.getName()));
                    editorEventManager.queueEvent(event);
                }
            }
        }
    }

    @Override
    public void doIt() {
        final Session session = getSession();
        if (session.getTimeline().getTierNames().contains(intervalTier.getName())
                || session.getTimeline().getRecordIntervalTiers().contains(intervalTier.getName())) {
            return;
        }
        if (session.getTimeline().addTier(intervalTier)) {
            // fire event
            final EditorEventManager editorEventManager = getEditorEventManager();
            if (editorEventManager != null) {
                final EditorEvent<EditorEventType.TimelineTierAddData> event = new EditorEvent<>(EditorEventType.TimelineTierAdd, getSource(),
                        new EditorEventType.TimelineTierAddData(this.intervalTier.getName()));
                editorEventManager.queueEvent(event);
            }
        }
    }


}
