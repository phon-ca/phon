package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;
import ca.phon.session.filter.RecordFilter;

public abstract class IntervalTierImporter {

    public int[] importTier(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, RecordFilter recordFilter) {
        return importTier(session, eventManager, Transcriber.VALIDATOR, undoSupport, recordFilter);
    }

    /**
     * Import tier with undo support and editor event manager
     *
     * @param session the session
     * @param eventManager the editor event manager
     * @param transcriber the transcriber
     * @param undoSupport the undo support
     * @param recordFilter the record filter
     * @return list of record indices that were modified/added
     */
    public abstract int[] importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, RecordFilter recordFilter);

}
