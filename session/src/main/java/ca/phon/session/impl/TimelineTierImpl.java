package ca.phon.session.impl;

import ca.phon.session.TimelineTier;
import ca.phon.session.spi.TimelineTierSPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of a timeline tier.
 */
public class TimelineTierImpl implements TimelineTierSPI {

    private String name;

    private List<TimelineTier.Interval> intervals;

    public TimelineTierImpl() {
        super();
        this.name = "";
        this.intervals = new ArrayList<>();
    }

    public TimelineTierImpl(String name) {
        super();
        this.name = name;
        this.intervals = new ArrayList<>();
    }

    public TimelineTierImpl(String name, List<TimelineTier.Interval> intervals) {
        super();
        this.name = name;
        this.intervals = intervals;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public List<TimelineTier.Interval> getIntervals() {
        return this.intervals;
    }

    @Override
    public boolean addInterval(TimelineTier.Interval interval, TimelineTier.InsertionStrategy insertionStrategy) {
        return this.intervals.add(interval);
    }

    @Override
    public boolean removeInterval(TimelineTier.Interval interval) {
        return this.intervals.remove(interval);
    }

}
