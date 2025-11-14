package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.IntervalTierSPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collection of interval/points
 */
public final class IntervalTier extends ExtendableObject {

    /**
     * Insertion strategy used when adding new intervals
     * to the tier and an overlap occurs.
     */
    public enum InsertionStrategy {
        /** Ignore overlaps */
        ALLOW_OVERLAPS,
        /** Throw IllegalArgumentException on overlap */
        ERROR_ON_OVERLAP,
        /**
         * Divide intervals on overlap.
         * Given interval A and B, with A being the interval inserted
         * <ul>
         *     <li>if A.start < B.start && A.end > B.start: B.start will be shifted to A.end</></li>
         *     <li>if A.start < B.end && A.end > B.end: B.end is shifted to A.start</li>
         *     <li>if A.start > B.start && A.end < B.end: new interval C(B.start, A.start) is created with empty label;
         *     B.start is shifted to A.end
         *     </li>
         * </ul>
         * */
        DIVIDE_INTERVALS_ON_OVERLAP
    };

    /**
     * Start time of timeline tier, this may be used to restrict the values returned by
     */

    private IntervalTierSPI spi;

    /**
     * Create a new timeline tier
     */
    public IntervalTier(IntervalTierSPI spi) {
        super();
        this.spi = spi;
    }


    /**
     * Get tier name
     *
     * @return tierName
     */
    public String getName() {
        return spi.getName();
    }

    /**
     * Get list of intervals in tier (points included) as an unmodifiable list
     *
     * @return list of all intervals and points in tier in order
     */
    public List<Interval> getIntervals() {
        return Collections.unmodifiableList(spi.getIntervals());
    }

    /**
     * Add new interval to tier
     *
     * @param start
     * @param end
     * @param insertionStrategy
     *
     * @return new interval if created, null otherwise
     * @throws IllegalArgumentException if insertionStrategy is ERROR_ON_OVERLAPS and
     *  the given interval overlaps an existing interval in the tier
     */
    public Interval addInterval(float start, float end, InsertionStrategy insertionStrategy) {
        return addInterval(start, end, "", insertionStrategy);
    }

    /**
     * Add new interval to tier
     *
     * @param start
     * @param end
     * @param label
     * @param insertionStrategy
     *
     * @return new interval if created, null otherwise
     * @throws IllegalArgumentException if insertionStrategy is ERROR_ON_OVERLAPS and
     *  the given interval overlaps an existing interval in the tier
     */
    public Interval addInterval(float start, float end, String label, InsertionStrategy insertionStrategy) {
        Interval retVal = new Interval(start, end, label);
        if(addInterval(retVal, insertionStrategy) >= 0)
            return retVal;
        return null;
    }

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
    public int addInterval(Interval interval, InsertionStrategy insertionStrategy) {
        return spi.addInterval(interval, insertionStrategy);
    }

    /**
     * Remove interval from tier
     *
     * @param interval
     */
    public boolean removeInterval(Interval interval) {
        return spi.removeInterval(interval);
    }

    /**
     * Get intervals which overlap the provided interval with the given overlap types
     * accepted.
     *
     * @param interval the interval to check against
     * @param overlapTypes the overlap types to accept
     *
     * @return list of overlapping interval indices (or non-overlapping if NO_OVERLAP is provided as the only overlap type)
     */
    public int[] overlappingIntervals(Interval interval, OverlapType... overlapTypes) {
        List<Integer> overlappingIndices = new ArrayList<>();
        for(int i = 0; i < getIntervals().size(); i++) {
            final Interval currentInterval = getIntervals().get(i);
            final OverlapType overlapType = interval.overlapType(currentInterval);
            for(OverlapType ot:overlapTypes) {
                if(ot == overlapType) {
                    overlappingIndices.add(i);
                }
            }
        }
        return overlappingIndices.stream().mapToInt(i->i).toArray();
    }

    /**
     * Interval overlap type
     */
    public enum OverlapType {
        NO_OVERLAP,
        PARTIAL_OVERLAP_START,
        PARTIAL_OVERLAP_END,
        FULLY_CONTAINS,
        IS_FULLY_CONTAINED
    };

    /**
     * Interval entity for timeline tier
     */
    public static class Interval {
        private float start;
        private float end;
        private String label;

        public Interval(float start, float end) {
            this(start, end, "");
        }

        public Interval(float start, float end, String label) {
            this.start = start;
            this.end = end;
            this.label = label;
        }

        public float getStart() {
            return start;
        }

        public void setStart(float start) {
            this.start = start;
        }

        public float getEnd() {
            return end;
        }

        public void setEnd(float end) {
            this.end = end;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isPoint() {
            return this.start == this.end;
        }

        public OverlapType overlapType(Interval other) {
            if( (this.end <= other.start) || (this.start >= other.end) ) {
                return OverlapType.NO_OVERLAP;
            } else if( (this.start < other.start) && (this.end < other.end) ) {
                return OverlapType.PARTIAL_OVERLAP_START;
            } else if( (this.start > other.start) && (this.end > other.end) ) {
                return OverlapType.PARTIAL_OVERLAP_END;
            } else if( (this.start <= other.start) && (this.end >= other.end) ) {
                return OverlapType.FULLY_CONTAINS;
            } else if( (this.start >= other.start) && (this.end <= other.end) ) {
                return OverlapType.IS_FULLY_CONTAINED;
            } else {
                return OverlapType.NO_OVERLAP;
            }
        }

    }

    /**
     * Point entity for timeline tier
     */
    public static class Point extends Interval {

        public Point(float point) {
            this(point, "");
        }

        public Point(float point, String label) {
            super(point, point, label);
        }

    }

}
