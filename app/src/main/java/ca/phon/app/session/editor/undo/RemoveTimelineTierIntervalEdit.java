package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.session.Session;
import ca.phon.session.TimelineTier;

import javax.swing.undo.CannotUndoException;

/**
 * Undoable edit for removing a timeline tier interval.
 */
public class RemoveTimelineTierIntervalEdit extends AddTimelineTierIntervalEdit {

    public RemoveTimelineTierIntervalEdit(Session session, EditorEventManager editorEventManager,
                                          String tierName, TimelineTier.Interval timelineTierInterval) {
        super(session, editorEventManager, tierName, timelineTierInterval);
    }

    @Override
    public String getPresentationName() {
        return "remove timeline tier interval: " + getTierName();
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
