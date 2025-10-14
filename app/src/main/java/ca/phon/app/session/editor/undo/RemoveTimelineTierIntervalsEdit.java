package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;
import java.util.List;

/**
 * Undoable edit for removing multiple intervals from a timeline tier in a batch operation.
 */
public class RemoveTimelineTierIntervalsEdit extends AddTimelineTierIntervalsEdit {

    public RemoveTimelineTierIntervalsEdit(Session session, EditorEventManager editorEventManager,
                                           String tierName, List<IntervalTier.Interval> intervals) {
        super(session, editorEventManager, tierName, intervals);
    }

    @Override
    public String getPresentationName() {
        return "Remove interval(s) from timeline tier: " + getTierName();
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


