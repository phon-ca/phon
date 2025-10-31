package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.Orthography;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.spi.IntervalTierSPI;
import ca.phon.session.tierdata.TierData;
import ca.phon.util.Range;

import java.util.ArrayList;
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

    public RecordIntervalTier(Session session, String tierName) {
        this.session = session;
        this.tierName = tierName;
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

        if(tier.getValue() instanceof TierData tierData) {
            if(UserTierType.PhoneIntervals.getPhonTierName().equals(tier.getName())) {
                return phoneIntervals(tierData);
            } else {
                return tierDataIntervals(tierData);
            }
        } else if(tier.getValue() instanceof Orthography orthography) {
            return orthographyIntervals(orthography);
        }
        return List.of();
    }

    /**
     * Get record index from given interval index.
     *
     * @param intervalIndex
     * @return record index, or -1 if not found
     */
    public int getRecordIndexFromIntervalIndex(int intervalIndex) {
        int currentIndex = 0;
        for(int i = 0; i < session.getRecordCount(); i++) {
            final Record record = session.getRecord(i);
            final List<IntervalTier.Interval> intervals = getIntervals(record);
            if(intervalIndex < currentIndex + intervals.size()) {
                return i;
            } else {
                currentIndex += intervals.size();
            }
        }
        return -1;
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
        return getRecordIntervalData(true).intervals;
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
        if(cachedRecordIntervalData == null) return;

        final List<IntervalTier.Interval> intervals = new ArrayList<>(cachedRecordIntervalData.intervals);
        final Map<Record, Range> recordRanges = new java.util.HashMap<>(cachedRecordIntervalData.recordRanges);

        final var recIntervals = getIntervals(record);
        final var range = recordRanges.get(record);
        if(range != null) {
            // check ranges
            if(range.getStart() < 0 || range.getStart() >= intervals.size()
                    || range.getEnd() < 0 || range.getEnd() >= intervals.size()
                    || range.getStart() > range.getEnd()) {
                return;
            }

            // replace existing intervals
            intervals.subList(range.getStart(), range.getEnd() + 1).clear();
            intervals.addAll(range.getStart(), recIntervals);

            // update ranges for subsequent records
            final int sizeDiff = recIntervals.size() - (range.getEnd() - range.getStart() + 1);
            if(sizeDiff != 0) {
                boolean found = false;
                for(var entry:recordRanges.entrySet()) {
                    if(entry.getKey().equals(record)) {
                        found = true;
                        recordRanges.put(entry.getKey(), new Range(entry.getValue().getStart(), entry.getValue().getStart() + recIntervals.size() - 1));
                    } else if(found) {
                        recordRanges.put(entry.getKey(), new Range(entry.getValue().getStart() + sizeDiff, entry.getValue().getEnd() + sizeDiff));
                    }
                }
            }
        } else {
            // add new record intervals at the end
            recordRanges.put(record, new Range(intervals.size(), intervals.size() + recIntervals.size() - 1));
            intervals.addAll(recIntervals);
        }

        cachedRecordIntervalData = new RecordIntervalData(intervals, recordRanges);
    }

    /**
     * Data structure holding record interval data.
     *
     * @param intervals all intervals
     * @param recordRanges interval ranges mapped to records
     */
    public record RecordIntervalData(List<IntervalTier.Interval> intervals, Map<Record, Range> recordRanges) {}

    private RecordIntervalData cachedRecordIntervalData = null;

    /**
     * Get intervals for all records along with their ranges in the
     * overall interval list.
     *
     * @param cached if true, use cached data if available
     * @return record interval data
     */
    public RecordIntervalData getRecordIntervalData(boolean cached) {
        if(cached && cachedRecordIntervalData != null) {
            return cachedRecordIntervalData;
        } else {
            final List<IntervalTier.Interval> intervals = new ArrayList<>();
            final Map<Record, Range> recordRanges = new java.util.HashMap<>();
            int currentIndex = 0;
            for (var record : session.getRecords()) {
                final var recIntervals = getIntervals(record);
                intervals.addAll(recIntervals);
                recordRanges.put(record, new Range(currentIndex, currentIndex + recIntervals.size() - 1));
                currentIndex += recIntervals.size();
            }
            final RecordIntervalData retVal = new RecordIntervalData(intervals, recordRanges);
            cachedRecordIntervalData = retVal;
            return retVal;
        }
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
