package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.session.*;
import ca.phon.session.Record;

import java.util.ArrayList;
import java.util.List;

public class RecordsFromIntervalTier {

    private final RecordsFromIntervalTierSettings settings;

    public RecordsFromIntervalTier(String intervalTierName, Participant speaker) {
        this(new RecordsFromIntervalTierSettings(intervalTierName, false, 0.0f, 0.0f, speaker));
    }

    public RecordsFromIntervalTier(String intervalTierName, boolean groupContiguousIntervals,
                                   float maxGapLength, float padding, Participant speaker) {
        this(new RecordsFromIntervalTierSettings(intervalTierName, groupContiguousIntervals, maxGapLength, padding, speaker));
    }

    public RecordsFromIntervalTier(RecordsFromIntervalTierSettings settings) {
        this.settings = settings;
    }

    /**
     * Create records from intervals in the specified session.
     *
     * @param session the session
     * @return list of media segments representing the records
     */
    public List<MediaSegment> segmentsFromSessionIntervals(Session session) {
        final IntervalTier intervalTier = session.getTimeline().getTier(settings.intervalTierName());
        if(intervalTier == null) {
            throw new IllegalArgumentException("Interval tier '" + settings.intervalTierName() + "' not found in session");
        }

        final SessionFactory factory = SessionFactory.newFactory();
        final List<MediaSegment> retVal = new ArrayList<>();
        float currentStart = -1.0f;
        float currentEnd = -1.0f;
        for(int i = 0; i < intervalTier.getIntervals().size(); i++) {
            final IntervalTier.Interval interval = intervalTier.getIntervals().get(i);
            if(currentStart < 0) {
                currentStart = interval.getStart();
                currentEnd = interval.getEnd();
            } else {
                final float gap = interval.getStart() - currentEnd;
                if(settings.groupContiguousIntervals() && gap <= settings.maxGapLength()) {
                    // extend current end
                    currentEnd = interval.getEnd();
                } else {
                    // save current segment
                    final MediaSegment segment = factory.createMediaSegment();
                    segment.setUnitType(MediaUnit.Second);
                    segment.setStartTime(Math.max(0, currentStart - settings.padding()));
                    segment.setEndTime(currentEnd + settings.padding());
                    retVal.add(segment);
                    // start new segment
                    currentStart = interval.getStart();
                    currentEnd = interval.getEnd();
                }
            }
        }
        // add last segment
        if(currentStart >= 0) {
            final MediaSegment segment = factory.createMediaSegment();
            segment.setUnitType(MediaUnit.Second);
            segment.setStartTime(Math.max(0, currentStart - settings.padding()));
            segment.setEndTime(currentEnd + settings.padding());
            retVal.add(segment);
        }
        return retVal;
    }

    /**
     * Create records from intervals in the specified session with undo support.
     *
     * @param session the session
     * @param eventManager event manager for session change events
     * @param undoSupport undo support
     * @param speaker optional participant to set as speaker for new records
     */
    public void createRecordsFromSessionIntervals(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport) {
        final List<MediaSegment> segments = segmentsFromSessionIntervals(session);
        final SessionFactory factory = SessionFactory.newFactory();
        undoSupport.beginUpdate("Create records from intervals");
        for(MediaSegment segment: segments) {
            final Record newRecord = factory.createRecord();
            newRecord.setMediaSegment(segment);
            newRecord.setSpeaker(settings.speaker());
            final AddRecordEdit addRecordEdit = new AddRecordEdit(session, eventManager, newRecord);
            undoSupport.postEdit(addRecordEdit);
        }
        undoSupport.endUpdate();
    }

}
