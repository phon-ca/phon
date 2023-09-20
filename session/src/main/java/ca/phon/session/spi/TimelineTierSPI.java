package ca.phon.session.spi;

import ca.phon.session.TimelineTier;

import java.util.List;

public interface TimelineTierSPI {

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
    public List<TimelineTier.Interval> getIntervals();

    /**
     * Add new interval to tier
     *
     * @param interval
     * @param insertionStrategy
     *
     * @return boolean if interval was added, false otherwise
     * @throws IllegalArgumentException if insertionStrategy is ERROR_ON_OVERLAPS and
     *  the given interval overlaps an existing interval in the tier
     */
    public boolean addInterval(TimelineTier.Interval interval, TimelineTier.InsertionStrategy insertionStrategy);

    /**
     * Remove interval from tier
     *
     * @param interval
     */
    public boolean removeInterval(TimelineTier.Interval interval);

}
