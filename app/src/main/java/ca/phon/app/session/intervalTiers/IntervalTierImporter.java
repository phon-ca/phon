package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;

public abstract class IntervalTierImporter {

    public void importTier(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, int recordStartIndex) {
        importTier(session, eventManager, Transcriber.VALIDATOR, undoSupport, recordStartIndex);
    }

    /**
     * Import tier with undo support and editor event manager
     *
     * @param session
     * @param eventManager
     * @param transcriber
     * @param undoSupport
     * @param recordStartIndex
     */
    public abstract void importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, int recordStartIndex);

}
