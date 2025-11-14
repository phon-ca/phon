package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;
import ca.phon.session.filter.RecordFilter;

public abstract class IntervalTierImporter {

    public void importTier(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, RecordFilter recordFilter) {
        importTier(session, eventManager, Transcriber.VALIDATOR, undoSupport, recordFilter);
    }

    /**
     * Import tier with undo support and editor event manager
     *
     * @param session
     * @param eventManager
     * @param transcriber
     * @param undoSupport
     * @param recordFilter
     */
    public abstract void importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, RecordFilter recordFilter);

}
