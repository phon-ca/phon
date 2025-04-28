package ca.phon.app.session.timeline;

import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.spi.IntervalTierSPI;
import ca.phon.session.tierdata.TierData;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for record tier data so it may be viewed as an interval tier.
 * The record tier should be of type {@link ca.phon.session.tierdata.TierData}
 * with intervals/points placed at the end of label data.
 *
 * e.g., hello •0.-999-1.28• world •1.28-2.08•
 */
public class RecordIntervalTier implements IntervalTierSPI {

    private final Session session;

    private final String tierName;

    public RecordIntervalTier(Session session, String tierName) {
        this.session = session;
        this.tierName = tierName;
    }

    @Override
    public String getName() {
        return tierName;
    }

    public List<IntervalTier.Interval> getIntervals(Record record) {
        final Tier<?> tier = record.getTier(this.tierName);
        // process tier data
        if(tier == null) return List.of();

        if(tier.getValue() instanceof TierData tierData) {
            return tierDataIntervals(tierData);
        } else if(tier.getValue() instanceof Orthography orthography) {
            return orthographyIntervals(orthography);
        }
        return List.of();
    }

    /**
     * Get intervals from the given {@link Orthography} object.
     *
     * @param orthography
     * @return
     */
    public static List<IntervalTier.Interval> orthographyIntervals(Orthography orthography) {
        final List<IntervalTier.Interval> intervals = new ArrayList<>();
        if(orthography == null) return List.of();

        final OrthoIntervalVisitor visitor = new OrthoIntervalVisitor();
        orthography.accept(visitor);

        return visitor.getIntervals();
    }

    /**
     * Get intervals from the given {@link TierData} object.
     *
     * @param tierData
     * @return
     */
    public static List<IntervalTier.Interval> tierDataIntervals(TierData tierData) {
        final List<IntervalTier.Interval> intervals = new ArrayList<>();
        if(tierData == null) return List.of();

        final TierDataIntervalVisitor visitor = new TierDataIntervalVisitor();
        tierData.accept(visitor);

        return visitor.getIntervals();
    }

    @Override
    public List<IntervalTier.Interval> getIntervals() {
        final List<IntervalTier.Interval> intervals = new ArrayList<>();
        for(var record:session.getRecords()) {
            intervals.addAll(getIntervals(record));
        }
        return intervals;
    }

    // not implemented
    @Override
    public boolean addInterval(IntervalTier.Interval interval, IntervalTier.InsertionStrategy insertionStrategy) {
        return false;
    }

    // not implemented
    @Override
    public boolean removeInterval(IntervalTier.Interval interval) {
        return false;
    }

}
