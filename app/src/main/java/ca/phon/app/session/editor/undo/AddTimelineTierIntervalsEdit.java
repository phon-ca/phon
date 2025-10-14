package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Undoable edit for adding multiple intervals to a timeline tier in a batch operation.
 */
public class AddTimelineTierIntervalsEdit extends SessionUndoableEdit {

    private String tierName;

    private List<IntervalTier.Interval> intervals;

    private List<IntervalTier.Interval> addedIntervals;

    public AddTimelineTierIntervalsEdit(Session session, EditorEventManager eventManager,
                                        String tierName, List<IntervalTier.Interval> intervals) {
        super(session, eventManager);
        this.tierName = tierName;
        this.intervals = new ArrayList<>(intervals);
        this.addedIntervals = new ArrayList<>();
    }

    public String getTierName() {
        return tierName;
    }

    @Override
    public String getPresentationName() {
        return "Add " + intervals.size() + " interval(s) to timeline tier: " + tierName;
    }

    @Override
    public void doIt() {
        final Session session = getSession();
        if (session.getTimeline().getTierNames().contains(tierName)) {
            final IntervalTier intervalTier = session.getTimeline().getTier(tierName);
            if(intervalTier == null) return;

            addedIntervals.clear();
            for (IntervalTier.Interval interval : intervals) {
                if (intervalTier.addInterval(interval, IntervalTier.InsertionStrategy.ALLOW_OVERLAPS)) {
                    addedIntervals.add(interval);
                }
            }

            // fire event for all added intervals
            if (!addedIntervals.isEmpty()) {
                final EditorEventManager editorEventManager = getEditorEventManager();
                if (editorEventManager != null) {
                    final EditorEvent<EditorEventType.TimelineIntervalsAddData> event = new EditorEvent<>(
                            EditorEventType.TimelineIntervalsAdd, getSource(),
                            new EditorEventType.TimelineIntervalsAddData(tierName, addedIntervals));
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

            List<IntervalTier.Interval> removedIntervals = new ArrayList<>();
            for (IntervalTier.Interval interval : addedIntervals) {
                if (intervalTier.removeInterval(interval)) {
                    removedIntervals.add(interval);
                }
            }

            // fire event for all removed intervals
            if (!removedIntervals.isEmpty()) {
                final EditorEventManager editorEventManager = getEditorEventManager();
                if (editorEventManager != null) {
                    final EditorEvent<EditorEventType.TimelineIntervalsRemoveData> event = new EditorEvent<>(
                            EditorEventType.TimelineIntervalsRemove, getSource(),
                            new EditorEventType.TimelineIntervalsRemoveData(tierName, removedIntervals));
                    editorEventManager.queueEvent(event);
                }
            }
        }
    }

}

