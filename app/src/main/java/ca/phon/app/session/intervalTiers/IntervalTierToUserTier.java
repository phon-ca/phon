package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.orthography.InternalMedia;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.filter.RecordFilter;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.session.tierdata.TierString;

import java.util.List;

/**
 * Import intervals from an interval tier into a user-defined tier. The intervals
 * that fully contain each record's media segment will be added
 * to the user tier for that record. Optionally, InternalMedia objects
 * can be added between the tier strings.
 */
public final class IntervalTierToUserTier extends IntervalTierImporter {

    private final IntervalTierToUserTierSettings settings;

    public IntervalTierToUserTier(IntervalTierToUserTierSettings settings) {
        this.settings = settings;
    }

    public int[] importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, RecordFilter recordFilter) {
        final IntervalTier importTier = session.getTimeline().getTier(settings.intervalTierName());
        if(importTier == null) {
            throw new IllegalArgumentException("Interval tier '" + settings.intervalTierName() + "' not found in session");
        }
        TierDescription tierDesc = session.getTier(settings.recordTierName());
        if(tierDesc == null) {
            final SessionFactory factory = SessionFactory.newFactory();
            tierDesc = factory.createTierDescription(settings.recordTierName());
            final TierViewItem tvi = factory.createTierViewItem(settings.recordTierName());
            // add tier to session
            final AddTierEdit addTierEdit = new AddTierEdit(session, eventManager, tierDesc, tvi);
            undoSupport.postEdit(addTierEdit);
        }

        final List<Integer> modifiedRecords = new java.util.ArrayList<>();
        for(int recordIndex = 0; recordIndex < session.getRecordCount(); recordIndex++) {
            final Record record = session.getRecord(recordIndex);
            if(!recordFilter.checkRecord(record)) continue;

            final MediaSegment segment = record.getMediaSegment();
            if(segment.isPoint()) continue;

            final IntervalTier.Interval recordInterval = new IntervalTier.Interval(segment.getStartTime(), segment.getEndTime());
            final int[] containedIntervals = importTier.overlappingIntervals(recordInterval, IntervalTier.OverlapType.FULLY_CONTAINS);
            if(containedIntervals.length == 0) continue;

            final TierData tierData = getTierElements(containedIntervals, importTier);
            final TierEdit<TierData> tierEdit = new TierEdit<>(session, eventManager, transcriber, record, record.getTier(settings.recordTierName(), TierData.class), tierData, false);
            undoSupport.postEdit(tierEdit);

            modifiedRecords.add(recordIndex);
        }

        return modifiedRecords.stream().mapToInt(i -> i).toArray();
    }

    private TierData getTierElements(int[] containedIntervals, IntervalTier importTier) {
        final List<TierElement> tierElements = new java.util.ArrayList<>();
        for(int i = 0; i < containedIntervals.length; i++) {
            final IntervalTier.Interval interval = importTier.getIntervals().get(containedIntervals[i]);
            final TierString label = new TierString(interval.getLabel());
            tierElements.add(label);
            if(settings.includeIntervalText()) {
                final TierElement internalMedia = new TierInternalMedia(new InternalMedia(interval.getStart(), interval.getEnd()));
                tierElements.add(internalMedia);
            }
        }
        final TierData tierData = new TierData(tierElements);
        return tierData;
    }

}
