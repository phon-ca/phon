package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;

/**
 * Undoable edit for removing a timeline tier.
 *
 */
public class RemoveTimelineTierEdit extends AddTimelineTierEdit {

    public RemoveTimelineTierEdit(Session session, EditorEventManager editorEventManager, IntervalTier intervalTier) {
        super(session, editorEventManager, intervalTier);
    }

    @Override
    public String getPresentationName() {
        return "remove timeline tier: " + intervalTier.getName();
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
