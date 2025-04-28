package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;

/**
 * Undoable edit for adding an interval to a timeline tier.
 */
public class AddTimelineTierIntervalEdit extends SessionUndoableEdit {

    private String tierName;

    private IntervalTier.Interval interval;

    public AddTimelineTierIntervalEdit(Session session, EditorEventManager eventManager,
                                       String tierName, IntervalTier.Interval interval) {
        super(session, eventManager);
        this.tierName = tierName;
        this.interval = interval;
    }

    public String getTierName() {
        return tierName;
    }

    @Override
    public String getPresentationName() {
        return "Add interval to timeline tier: " + tierName;
    }

    @Override
    public void doIt() {
        final Session session = getSession();
        if (session.getTimeline().getTierNames().contains(tierName)) {
            final IntervalTier intervalTier = session.getTimeline().getTier(tierName);
            if(intervalTier == null) return;
            if (intervalTier.addInterval(this.interval, IntervalTier.InsertionStrategy.ALLOW_OVERLAPS)) {
                // fire event
                final EditorEventManager editorEventManager = getEditorEventManager();
                if (editorEventManager != null) {
                    final EditorEvent<EditorEventType.TimelineIntervalAddData> event = new EditorEvent<>(EditorEventType.TimelineIntervalAdd, getSource(),
                            new EditorEventType.TimelineIntervalAddData(tierName, interval));
                    editorEventManager.queueEvent(event);
                }
            }
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        final Session session = getSession();
        if (session.getTimeline().getTierNames().contains(tierName)) {
            final IntervalTier intervalTier = session.getTimeline().getTier(tierName);
            if(intervalTier == null) return;
            if (intervalTier.removeInterval(this.interval)) {
                // fire event
                final EditorEventManager editorEventManager = getEditorEventManager();
                if (editorEventManager != null) {
                    final EditorEvent<EditorEventType.TimelineIntervalRemoveData> event = new EditorEvent<>(EditorEventType.TimelineIntervalRemove, getSource(),
                            new EditorEventType.TimelineIntervalRemoveData(tierName, interval));
                    editorEventManager.queueEvent(event);
                }
            }
        }
    }

}
