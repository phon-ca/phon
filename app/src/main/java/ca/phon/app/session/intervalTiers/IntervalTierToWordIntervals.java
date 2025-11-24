package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.orthography.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.filter.RecordFilter;
import ca.phon.util.SegmentOverlapUtil.OverlapType;

import java.util.ArrayList;
import java.util.List;

/**
 * Import intervals from an interval tier into a word intervals tier (WOR). The intervals
 * that fully contain each record's media segment will be concatenated
 * and added to the word tier for that record with InternalMedia objects between words.
 * A terminator can also be added to the end of the word interval tier.
 */
public final class IntervalTierToWordIntervals extends IntervalTierImporter {

    private final IntervalTierToWordIntervalsSettings settings;

    public IntervalTierToWordIntervals(String intervalTierName) {
        this(new IntervalTierToWordIntervalsSettings(intervalTierName, false, TerminatorType.PERIOD));
    }

    public IntervalTierToWordIntervals(IntervalTierToWordIntervalsSettings settings) {
        this.settings = settings;
    }

    public IntervalTierToWordIntervals(String intervalTierName,
                                      boolean addTerminator) {
        this(new IntervalTierToWordIntervalsSettings(intervalTierName, addTerminator, TerminatorType.PERIOD));
    }

    public IntervalTierToWordIntervals(String intervalTierName,
                                      boolean addTerminator,
                                      String terminator) {
        this(new IntervalTierToWordIntervalsSettings(intervalTierName, addTerminator, TerminatorType.fromString(terminator)));
    }

    public IntervalTierToWordIntervals(String intervalTierName,
                                      boolean addTerminator,
                                      TerminatorType terminatorType) {
        this(new IntervalTierToWordIntervalsSettings(intervalTierName, addTerminator, terminatorType));
    }

    @Override
    public int[] importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, RecordFilter recordFilter) {
        final IntervalTier importTier = session.getTimeline().getTier(settings.intervalTierName());
        if(importTier == null) {
            throw new IllegalArgumentException("Interval tier '" + settings.intervalTierName() + "' not found in session");
        }

        // Ensure WOR tier exists
        TierDescription worTierDesc = session.getTier(UserTierType.Wor.getPhonTierName());
        if(worTierDesc == null) {
            final SessionFactory factory = SessionFactory.newFactory();
            worTierDesc = factory.createTierDescription(UserTierType.Wor);
            final var tvi = factory.createTierViewItem(UserTierType.Wor.getPhonTierName());
            final var addTierEdit = new AddTierEdit(session, eventManager, worTierDesc, tvi);
            undoSupport.postEdit(addTierEdit);
        }

        final List<Integer> updatedRecords = new ArrayList<>();
        for(int recordIndex = 0; recordIndex < session.getRecordCount(); recordIndex++) {
            final Record record = session.getRecord(recordIndex);
            if(!recordFilter.checkRecord(record)) continue;

            final OrthographyBuilder worBuilder = new OrthographyBuilder();
            final MediaSegment segment = record.getMediaSegment();
            if(segment.isPoint()) continue;

            final IntervalTier.Interval recordInterval = new IntervalTier.Interval(segment.getStartTime(), segment.getEndTime());
            final int[] containedIntervals = importTier.overlappingIntervals(recordInterval, OverlapType.FULLY_CONTAINS);
            if(containedIntervals.length == 0) continue;

            for(int intervalIdx : containedIntervals) {
                final IntervalTier.Interval interval = importTier.getIntervals().get(intervalIdx);
                String label = interval.getLabel().trim();
                if(!label.isEmpty()) {
                    if(worBuilder.size() > 0) {
                        worBuilder.append(" ");
                    }
                    worBuilder.append(label);
                    worBuilder.append(" ");
                    // Create a new InternalMedia object for the word interval
                    final InternalMedia im = new InternalMedia(interval.getStart(), interval.getEnd());
                    worBuilder.append(im);
                }
            }

            if(settings.addTerminator()) {
                if(!worBuilder.toOrthography().hasTerminator()) {
                    worBuilder.append(new Terminator(settings.terminatorType()));
                }
            }

            final Orthography worOrthography = worBuilder.toOrthography();
            final TierEdit<Orthography> worTierEdit = new TierEdit<>(session, eventManager, transcriber, record,
                    (Tier<Orthography>)record.getTier(UserTierType.Wor.getPhonTierName()), worOrthography, false);
            undoSupport.postEdit(worTierEdit);

            updatedRecords.add(recordIndex);
        }

        return updatedRecords.stream().mapToInt(i -> i).toArray();
    }

}

