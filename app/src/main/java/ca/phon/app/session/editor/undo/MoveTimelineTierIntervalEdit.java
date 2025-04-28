package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;

/**
 * Undoable edit for moving a timeline tier interval.
 */
public class MoveTimelineTierIntervalEdit extends SessionUndoableEdit {

    private String tierName;

    private IntervalTier.Interval interval;

    private float oldStartTime;

    private float oldEndTime;

    private float newStartTime;

    private float newEndTime;

    public MoveTimelineTierIntervalEdit(Session session, EditorEventManager eventManager,
                                        String tierName, IntervalTier.Interval interval,
                                        float newStartTime, float newEndTime) {
        super(session, eventManager);
        this.tierName = tierName;
        this.interval = interval;
        this.oldStartTime = interval.getStart();
        this.oldEndTime = interval.getEnd();
        this.newStartTime = newStartTime;
        this.newEndTime = newEndTime;
    }

    public String getTierName() {
        return tierName;
    }

    @Override
    public String getPresentationName() {
        return "Move interval in timeline tier: " + tierName;
    }

    @Override
    public void doIt() {
        final Session session = getSession();
        if (session.getTimeline().getTierNames().contains(tierName)) {
            final IntervalTier intervalTier = session.getTimeline().getTier(tierName);
            if(intervalTier == null) return;
            // ensure interval is in tier
            if (intervalTier.getIntervals().contains(this.interval)) {
                // adjust interval
                interval.setStart(newStartTime);
                interval.setEnd(newEndTime);

                // fire event
                final EditorEventManager editorEventManager = getEditorEventManager();
                if (editorEventManager != null) {
                    final EditorEvent<EditorEventType.TimelineIntervalMoveData> event = new EditorEvent<>(EditorEventType.TimelineIntervalMove, getSource(),
                            new EditorEventType.TimelineIntervalMoveData(tierName, interval, oldStartTime, oldEndTime, newStartTime, newEndTime));
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
            // ensure interval is in tier
            if (intervalTier.getIntervals().contains(this.interval)) {
                // adjust interval
                interval.setStart(oldStartTime);
                interval.setEnd(oldEndTime);

                // fire event
                final EditorEventManager editorEventManager = getEditorEventManager();
                if (editorEventManager != null) {
                    final EditorEvent<EditorEventType.TimelineIntervalMoveData> event = new EditorEvent<>(EditorEventType.TimelineIntervalMove, getSource(),
                            new EditorEventType.TimelineIntervalMoveData(tierName, interval, newStartTime, newEndTime, oldStartTime, oldEndTime));
                    editorEventManager.queueEvent(event);
                }
            }
        }
    }

}
