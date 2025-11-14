package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.orthography.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.filter.RecordFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Import intervals from an interval tier into an orthography tier. The intervals
 * that fully contain each record's media segment will be concatenated
 * and added to the orthography tier for that record. Optionally, a word
 * tier can also be populated with the same content but with InternalMedia
 * objects between words. A terminator can also be added to the end of the
 * orthography and word interval tiers.
 */
public final class IntervalTierToOrthography extends IntervalTierImporter {

    private final IntervalTierToOrthographySettings settings;

    public IntervalTierToOrthography(String intervalTierName, boolean importWorTier) {
        this(new IntervalTierToOrthographySettings(intervalTierName,
                importWorTier,
                false,
                TerminatorType.PERIOD));
    }

    public IntervalTierToOrthography(IntervalTierToOrthographySettings settings) {
        this.settings = settings;
    }

    public IntervalTierToOrthography(String intervalTierName,
                                     boolean importWorTier,
                                     boolean addTerminator) {
        this(new IntervalTierToOrthographySettings(intervalTierName,
                importWorTier,
                addTerminator,
                TerminatorType.PERIOD));
    }

    public IntervalTierToOrthography(String intervalTierName,
                                     boolean importWorTier,
                                     boolean addTerminator,
                                     String terminator) {
        this(new IntervalTierToOrthographySettings(intervalTierName,
                importWorTier,
                addTerminator,
                TerminatorType.fromString(terminator)));
    }

    public IntervalTierToOrthography(String intervalTierName,
                                     boolean importWorTier,
                                     boolean addTerminator,
                                     TerminatorType terminatorType) {
        this(new IntervalTierToOrthographySettings(intervalTierName,
                importWorTier,
                addTerminator,
                terminatorType));
    }

    @Override
    public int[] importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, RecordFilter recordFilter) {
        final IntervalTier importTier = session.getTimeline().getTier(settings.intervalTierName());
        if(importTier == null) {
            throw new IllegalArgumentException("Interval tier '" + settings.intervalTierName() + "' not found in session");
        }

        if(settings.importWorTier()) {
            TierDescription worTierDesc = session.getTier(UserTierType.Wor.getPhonTierName());
            if(worTierDesc == null) {
                final SessionFactory factory = SessionFactory.newFactory();
                // add wor tier to session
                final TierDescription newWorTierDesc = factory.createTierDescription(UserTierType.Wor);
                final var tvi = factory.createTierViewItem(UserTierType.Wor.getPhonTierName());
                final var addTierEdit = new ca.phon.app.session.editor.undo.AddTierEdit(session, eventManager, newWorTierDesc, tvi);
                undoSupport.postEdit(addTierEdit);
            }
        }

        final List<Integer> updatedRecords = new ArrayList<>();
        for(int recordIndex = 0; recordIndex < session.getRecordCount(); recordIndex++) {
            final Record record = session.getRecord(recordIndex);
            if(!recordFilter.checkRecord(record)) continue;

            final OrthographyBuilder builder = new OrthographyBuilder();
            final OrthographyBuilder worBuilder = new OrthographyBuilder();
            final MediaSegment segment = record.getMediaSegment();
            if(segment.isPoint()) continue;

            final IntervalTier.Interval recordInterval = new IntervalTier.Interval(segment.getStartTime(), segment.getEndTime());
            final int[] containedIntervals = importTier.overlappingIntervals(recordInterval, IntervalTier.OverlapType.FULLY_CONTAINS);
            if(containedIntervals.length == 0) continue;

            for(int intervalIdx:containedIntervals) {
                final IntervalTier.Interval interval = importTier.getIntervals().get(intervalIdx);
                String label = interval.getLabel().trim();
                if(!label.isEmpty()) {
                    if(builder.size() > 0) {
                        builder.append(" ");
                        worBuilder.append(" ");
                    }
                    builder.append(label);

                    worBuilder.append(label);
                    worBuilder.append(" ");
                    // create a new InternalMedia object
                    final InternalMedia im = new InternalMedia(interval.getStart(), interval.getEnd());
                    worBuilder.append(im);
                }
            }

            if(settings.addTerminator()) {
                if(!builder.toOrthography().hasTerminator()) {
                    builder.append(new Terminator(settings.terminatorType()));
                }
            }
            final Orthography orthography = builder.toOrthography();
            final TierEdit<Orthography> orthoTierEdit = new TierEdit<>(session, eventManager, transcriber, record, record.getOrthographyTier(), orthography, false);
            undoSupport.postEdit(orthoTierEdit);

            updatedRecords.add(recordIndex);

            if(settings.importWorTier()) {
                if (settings.addTerminator()) {
                    worBuilder.append(new Terminator(settings.terminatorType()));
                }
                final Orthography worOrthography = worBuilder.toOrthography();
                final TierEdit<Orthography> worTierEdit = new TierEdit<>(session, eventManager, transcriber, record, (Tier<Orthography>)record.getTier(UserTierType.Wor.getPhonTierName()), worOrthography, false);
                undoSupport.postEdit(worTierEdit);
            }
        }

        return updatedRecords.stream().mapToInt(i -> i).toArray();
    }

}
