package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.session.*;
import ca.phon.session.Record;

import java.util.ArrayList;
import java.util.List;

public final class IntervalTierToRecordSegments extends IntervalTierImporter {

    private final IntervalTierToRecordSegmentsSettings settings;

    public IntervalTierToRecordSegments(String intervalTierName, Participant speaker) {
        this(new IntervalTierToRecordSegmentsSettings(intervalTierName, false, 0.0f, 0.0f, speaker, false));
    }

    public IntervalTierToRecordSegments(String intervalTierName, boolean groupContiguousIntervals,
                                        float maxGapLength, float padding, Participant speaker, boolean overwriteExistingRecords) {
        this(new IntervalTierToRecordSegmentsSettings(intervalTierName, groupContiguousIntervals, maxGapLength, padding, speaker, overwriteExistingRecords));
    }

    public IntervalTierToRecordSegments(IntervalTierToRecordSegmentsSettings settings) {
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
     * @param transcriber the transcriber
     * @param undoSupport undo support
     */
    @Override
    public void importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, int recordStartIndex) {
        final List<MediaSegment> segments = segmentsFromSessionIntervals(session);
        final SessionFactory factory = SessionFactory.newFactory();
        int recordIndex = recordStartIndex;
        for(MediaSegment segment: segments) {
            boolean recordExists = recordIndex < session.getRecordCount();
            final Record record = (recordExists && settings.overwriteExistingRecords()) ? session.getRecord(recordIndex) : factory.createRecord();
            if(recordExists && settings.overwriteExistingRecords()) {
                final TierEdit<MediaSegment> tierEdit =
                        new TierEdit<>(session, eventManager, transcriber, record, record.getSegmentTier(), segment);
                undoSupport.postEdit(tierEdit);
            } else {
                record.setMediaSegment(segment);
                record.setSpeaker(settings.speaker());
                final AddRecordEdit addRecordEdit = new AddRecordEdit(session, eventManager, record, recordIndex);
                undoSupport.postEdit(addRecordEdit);
            }
            recordIndex++;
        }
    }

}
