package ca.phon.session.spi;

import ca.phon.session.IntervalTier;

import java.util.List;

public interface IntervalTierSPI {

    /**
     * Get tier name
     *
     * @return tierName
     */
    public String getName();

    /**
     * Get list of intervals in tier (points included)
     *
     * @return list of all intervals and points in tier in order
     */
    public List<IntervalTier.Interval> getIntervals();

    /**
     * Add new interval to tier
     *
     * @param interval
     * @param insertionStrategy
     *
     * @return index of new interval added
     * @throws IllegalArgumentException if insertionStrategy is ERROR_ON_OVERLAPS and
     *  the given interval overlaps an existing interval in the tier
     */
    public int addInterval(IntervalTier.Interval interval, IntervalTier.InsertionStrategy insertionStrategy);

    /**
     * Remove interval from tier
     *
     * @param interval
     */
    public boolean removeInterval(IntervalTier.Interval interval);

    /**
     * Remove interval at given index from tier
     *
     * @param index the index of the interval to remove
     * @return the removed interval
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public IntervalTier.Interval removeIntervalAt(int index);

}
