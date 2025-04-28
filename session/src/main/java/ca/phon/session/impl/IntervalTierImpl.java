package ca.phon.session.impl;

import ca.phon.session.IntervalTier;
import ca.phon.session.spi.IntervalTierSPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of a timeline tier.
 */
public class IntervalTierImpl implements IntervalTierSPI {

    private String name;

    private List<IntervalTier.Interval> intervals;

    public IntervalTierImpl() {
        super();
        this.name = "";
        this.intervals = new ArrayList<>();
    }

    public IntervalTierImpl(String name) {
        super();
        this.name = name;
        this.intervals = new ArrayList<>();
    }

    public IntervalTierImpl(String name, List<IntervalTier.Interval> intervals) {
        super();
        this.name = name;
        this.intervals = intervals;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<IntervalTier.Interval> getIntervals() {
        return this.intervals;
    }

    @Override
    public boolean addInterval(IntervalTier.Interval interval, IntervalTier.InsertionStrategy insertionStrategy) {
        return this.intervals.add(interval);
    }

    @Override
    public boolean removeInterval(IntervalTier.Interval interval) {
        return this.intervals.remove(interval);
    }

}
