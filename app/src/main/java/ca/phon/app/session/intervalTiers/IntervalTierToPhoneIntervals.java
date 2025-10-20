package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.orthography.InternalMedia;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.session.tierdata.TierString;

import java.util.ArrayList;
import java.util.List;

/**
 * Import intervals from an interval tier into a phone intervals tier. The intervals
 * that are fully contained within each word interval will be concatenated
 * and added to the phone intervals tier for that record.  The word interval tier
 * must exist in the session and be populated for the record.
 */
public final class IntervalTierToPhoneIntervals extends IntervalTierImporter {

    private final IntervalTierToPhoneIntervalsSettings settings;

    public IntervalTierToPhoneIntervals(IntervalTierToPhoneIntervalsSettings settings) {
        this.settings = settings;
    }

    public void importTier(Session session, EditorEventManager eventManager, Transcriber transcriber, SessionEditUndoSupport undoSupport, int recordStartIndex) {
        final IntervalTier importTier = session.getTimeline().getTier(settings.intervalTierName());
        if(importTier == null) {
            throw new IllegalArgumentException("Interval tier '" + settings.intervalTierName() + "' not found in session");
        }
        undoSupport.beginUpdate("Import phone intervals from interval tier");

        TierDescription worTierDesc = session.getTier(UserTierType.Wor.getPhonTierName());
        if(worTierDesc == null) {
            throw new IllegalStateException("WOR tier not found in session");
        }

        TierDescription phoTierDesc = session.getTier(UserTierType.PhoneIntervals.getPhonTierName());
        if(phoTierDesc == null) {
            final SessionFactory factory = SessionFactory.newFactory();
            phoTierDesc = factory.createTierDescription(UserTierType.PhoneIntervals);
            final var tvi = factory.createTierViewItem(UserTierType.PhoneIntervals.getPhonTierName());
            final var addTierEdit = new AddTierEdit(session, eventManager, phoTierDesc, tvi);
            undoSupport.postEdit(addTierEdit);
        }

        for(int recordIndex = recordStartIndex; recordIndex < session.getRecordCount(); recordIndex++) {
            final Record record = session.getRecord(recordIndex);
            final Tier<Orthography> worTier = record.getTier(UserTierType.Wor.getPhonTierName(), Orthography.class);
            if(worTier != null) {
                final Orthography worData = worTier.getValue();
                final OrthoIntervalVisitor visitor = new OrthoIntervalVisitor();
                worData.accept(visitor);

                final List<InternalMedia> wordIntervals = visitor.getInternalMediaList();
                final List<TierElement> phoTierElements = new ArrayList<>();

                for(InternalMedia worInterval:wordIntervals) {
                    // get phone intervals which are contained withing the word interval
                    final IntervalTier.Interval wordInterval = new IntervalTier.Interval(worInterval.getStartTime(), worInterval.getEndTime());
                    final int[] containedIntervals = importTier.overlappingIntervals(wordInterval, IntervalTier.OverlapType.FULLY_CONTAINS);
                    if(containedIntervals.length > 0) {
                        if(!phoTierElements.isEmpty()) {
                            final TierString separator = new TierString("/");
                            phoTierElements.add(separator);
                        }
                        for(int i = 0; i < containedIntervals.length; i++) {
                            final IntervalTier.Interval phoneInterval = importTier.getIntervals().get(containedIntervals[i]);
                            final TierString tierPhoneInterval = new TierString(phoneInterval.getLabel());
                            phoTierElements.add(tierPhoneInterval);
                            final InternalMedia internalMedia = new InternalMedia(phoneInterval.getStart(), phoneInterval.getEnd());
                            final TierInternalMedia tierInternalMedia = new TierInternalMedia(internalMedia);
                            phoTierElements.add(tierInternalMedia);
                        }
                    }
                }
                final TierData phoTierData = new TierData(phoTierElements);
                final TierEdit<TierData> tierEdit =
                        new TierEdit<>(session, eventManager, transcriber, record,
                                record.getTier(UserTierType.PhoneIntervals.getPhonTierName(), TierData.class), phoTierData);
                undoSupport.postEdit(tierEdit);
            }
        }

        undoSupport.endUpdate();
    }

}
