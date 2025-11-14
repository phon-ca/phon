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
    public int addInterval(IntervalTier.Interval interval, IntervalTier.InsertionStrategy insertionStrategy) {
        int insertIdx = -1;
        for (int i = 0; i < this.intervals.size(); i++) {
            final IntervalTier.Interval currentInterval = this.intervals.get(i);
            if (interval.getEnd() <= currentInterval.getStart()) {
                insertIdx = i;
                break;
            } else if (interval.getStart() < currentInterval.getEnd()) {
                // overlap detected
                if (insertionStrategy == IntervalTier.InsertionStrategy.ERROR_ON_OVERLAP) {
                    throw new IllegalArgumentException("Interval overlaps existing interval in tier");
                } else if(insertionStrategy == IntervalTier.InsertionStrategy.ALLOW_OVERLAPS) {
                    insertIdx = i;
                    break;
                } else if(insertionStrategy == IntervalTier.InsertionStrategy.DIVIDE_INTERVALS_ON_OVERLAP) {
                    // TODO implement divide intervals on overlap
                }
            }
        }
        if (insertIdx == -1) {
            insertIdx = this.intervals.size();
        }
        this.intervals.add(insertIdx, interval);
        return insertIdx;
    }

    @Override
    public boolean removeInterval(IntervalTier.Interval interval) {
        return this.intervals.remove(interval);
    }

    @Override
    public IntervalTier.Interval removeIntervalAt(int index) {
        return this.intervals.remove(index);
    }

}
