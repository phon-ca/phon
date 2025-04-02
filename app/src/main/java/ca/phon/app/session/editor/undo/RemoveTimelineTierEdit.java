package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.session.Session;
import ca.phon.session.TimelineTier;

import javax.swing.undo.CannotUndoException;

/**
 * Undoable edit for removing a timeline tier.
 *
 */
public class RemoveTimelineTierEdit extends AddTimelineTierEdit {

    public RemoveTimelineTierEdit(Session session, EditorEventManager editorEventManager, TimelineTier timelineTier) {
        super(session, editorEventManager, timelineTier);
    }

    @Override
    public String getPresentationName() {
        return "remove timeline tier: " + timelineTier.getName();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.doIt();
    }

    @Override
    public void doIt() {
        super.undo();
    }

}
