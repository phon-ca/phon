package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.spi.IntervalTierSPI;
import ca.phon.session.tierdata.TierData;
import ca.phon.util.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private final Transcriber transcriber;

    public RecordIntervalTier(Session session, String tierName, Transcriber transcriber) {
        this.session = session;
        this.tierName = tierName;
        this.transcriber = transcriber;
    }

    @Override
    public String getName() {
        return tierName;
    }

    /**
     * Get intervals from the given {@link Record} object.
     *
     * @param record the record
     * @return list of intervals for the record, or empty list if none
     */
    public List<IntervalTier.Interval> getIntervals(Record record) {
        final Tier<?> tier = record.getTier(this.tierName);
        // process tier data
        if(tier == null) return List.of();

        if(tier.getDeclaredType() == TierData.class) {
            final TierData tierData = ((Tier<TierData>)tier).getValueForTranscriber(transcriber).orElse(new TierData());
            if(UserTierType.PhoneIntervals.getPhonTierName().equals(tier.getName())) {
                return phoneIntervals(tierData);
            } else {
                return tierDataIntervals(tierData);
            }
        } else if(tier.getDeclaredType() == Orthography.class) {
            final Orthography orthography = ((Tier<Orthography>)tier).getValueForTranscriber(transcriber).orElse(new Orthography());
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

    public static List<IntervalTier.Interval> phoneIntervals(TierData tierData) {
        final List<IntervalTier.Interval> intervals = new ArrayList<>();
        if(tierData == null) return List.of();

        final TierDataIntervalVisitor visitor = new TierDataIntervalVisitor();
        visitor.addIgnoreWord("/");
        tierData.accept(visitor);

        return visitor.getIntervals();
    }

    @Override
    public List<IntervalTier.Interval> getIntervals() {
        final Map<Record, List<IntervalTier.Interval>> recordIntervals =
                getRecordIntervalData(true);
        final List<IntervalTier.Interval> intervals = new ArrayList<>();
        for(Record record:session.getRecords()) {
            intervals.addAll(recordIntervals.get(record));
        }
        return Collections.unmodifiableList(intervals);
    }

    /**
     * Update cached interval data.
     */
    public void updateCachedIntervals() {
        getRecordIntervalData(false);
    }

    /**
     * Update cached interval data for a single record only.
     *
     * @param record
     *
     * @param record
     */
    public void updateCachedIntervals(Record record) {
        if(cachedRecordIntervalData == null) {
            updateCachedIntervals();
            return;
        }
        cachedRecordIntervalData.put(record, getIntervals(record));
    }

    /**
     * Get range of intervals for the given record in the overall
     * interval list.
     * @param record
     * @return range of intervals for record, or null if record has no intervals or Range(-1, -1) if record not found
     */
    public Range getIntervalRangeForRecord(Record record) {
        final Map<Record, List<IntervalTier.Interval>> cachedRecordIntervalData =
                getRecordIntervalData(true);
        if(cachedRecordIntervalData.containsKey(record)) {
            final List<IntervalTier.Interval> intervals = cachedRecordIntervalData.get(record);
            if(intervals.size() > 0) {
                int startIdx = 0;
                for(Record rec:session.getRecords()) {
                    if(rec.equals(record)) {
                        break;
                    } else {
                        startIdx += cachedRecordIntervalData.get(rec).size();
                    }
                }
                final int endIdx = startIdx + intervals.size();
                return new Range(startIdx, endIdx, true);
            }
        }
        return new Range(-1, -1, true);
    }


    private Map<Record, List<IntervalTier.Interval>> cachedRecordIntervalData = null;

    /**
     * Get intervals for all records along with their ranges in the
     * overall interval list.
     *
     * @param cached if true, use cached data if available
     * @return record interval data
     */
    public Map<Record, List<IntervalTier.Interval>> getRecordIntervalData(boolean cached) {
        if(cached && cachedRecordIntervalData != null) {
            return cachedRecordIntervalData;
        } else {
            if(cachedRecordIntervalData == null)
                cachedRecordIntervalData = new java.util.HashMap<>();
             else
                cachedRecordIntervalData.clear();
            for(Record record:session.getRecords()) {
                cachedRecordIntervalData.put(record, getIntervals(record));
            }
            return cachedRecordIntervalData;
        }
    }

    // not implemented
    @Override
    public int addInterval(IntervalTier.Interval interval, IntervalTier.InsertionStrategy insertionStrategy) {
        return -1;
    }

    // not implemented
    @Override
    public boolean removeInterval(IntervalTier.Interval interval) {
        return false;
    }

    // not implemented
    @Override
    public IntervalTier.Interval removeIntervalAt(int index) {
        return null;
    }

}
