/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.util;

/**
 * Utility class for computing overlap between segments (intervals).
 * A segment is defined by a start and end value where start <= end.
 */
public final class SegmentOverlapUtil {

    /**
     * Segment overlap type
     */
    public enum OverlapType {
        /** No overlap between segments */
        NO_OVERLAP,
        /** This segment partially overlaps at the start of the other segment */
        PARTIAL_OVERLAP_START,
        /** This segment partially overlaps at the end of the other segment */
        PARTIAL_OVERLAP_END,
        /** This segment fully contains the other segment */
        FULLY_CONTAINS,
        /** This segment is fully contained by the other segment */
        IS_FULLY_CONTAINED
    }

    /**
     * Private constructor to prevent instantiation
     */
    private SegmentOverlapUtil() {
    }

    /**
     * Compute the overlap type between two segments defined by their start and end values.
     *
     * @param thisStart the start value of the first segment
     * @param thisEnd the end value of the first segment
     * @param otherStart the start value of the second segment
     * @param otherEnd the end value of the second segment
     * @return the overlap type between the two segments
     */
    public static OverlapType computeOverlap(float thisStart, float thisEnd, float otherStart, float otherEnd) {
        if ((thisEnd <= otherStart) || (thisStart >= otherEnd)) {
            return OverlapType.NO_OVERLAP;
        } else if ((thisStart < otherStart) && (thisEnd < otherEnd)) {
            return OverlapType.PARTIAL_OVERLAP_START;
        } else if ((thisStart > otherStart) && (thisEnd > otherEnd)) {
            return OverlapType.PARTIAL_OVERLAP_END;
        } else if ((thisStart <= otherStart) && (thisEnd >= otherEnd)) {
            return OverlapType.FULLY_CONTAINS;
        } else if ((thisStart >= otherStart) && (thisEnd <= otherEnd)) {
            return OverlapType.IS_FULLY_CONTAINED;
        } else {
            return OverlapType.NO_OVERLAP;
        }
    }

    /**
     * Compute the overlap type between two segments defined by their start and end values.
     *
     * @param thisStart the start value of the first segment
     * @param thisEnd the end value of the first segment
     * @param otherStart the start value of the second segment
     * @param otherEnd the end value of the second segment
     * @return the overlap type between the two segments
     */
    public static OverlapType computeOverlap(double thisStart, double thisEnd, double otherStart, double otherEnd) {
        if ((thisEnd <= otherStart) || (thisStart >= otherEnd)) {
            return OverlapType.NO_OVERLAP;
        } else if ((thisStart < otherStart) && (thisEnd < otherEnd)) {
            return OverlapType.PARTIAL_OVERLAP_START;
        } else if ((thisStart > otherStart) && (thisEnd > otherEnd)) {
            return OverlapType.PARTIAL_OVERLAP_END;
        } else if ((thisStart <= otherStart) && (thisEnd >= otherEnd)) {
            return OverlapType.FULLY_CONTAINS;
        } else if ((thisStart >= otherStart) && (thisEnd <= otherEnd)) {
            return OverlapType.IS_FULLY_CONTAINED;
        } else {
            return OverlapType.NO_OVERLAP;
        }
    }

    /**
     * Compute the overlap type between two segments defined by their start and end values.
     *
     * @param thisStart the start value of the first segment
     * @param thisEnd the end value of the first segment
     * @param otherStart the start value of the second segment
     * @param otherEnd the end value of the second segment
     * @return the overlap type between the two segments
     */
    public static OverlapType computeOverlap(int thisStart, int thisEnd, int otherStart, int otherEnd) {
        if ((thisEnd <= otherStart) || (thisStart >= otherEnd)) {
            return OverlapType.NO_OVERLAP;
        } else if ((thisStart < otherStart) && (thisEnd < otherEnd)) {
            return OverlapType.PARTIAL_OVERLAP_START;
        } else if ((thisStart > otherStart) && (thisEnd > otherEnd)) {
            return OverlapType.PARTIAL_OVERLAP_END;
        } else if ((thisStart <= otherStart) && (thisEnd >= otherEnd)) {
            return OverlapType.FULLY_CONTAINS;
        } else if ((thisStart >= otherStart) && (thisEnd <= otherEnd)) {
            return OverlapType.IS_FULLY_CONTAINED;
        } else {
            return OverlapType.NO_OVERLAP;
        }
    }

    /**
     * Compute the overlap type between two segments defined by their start and end values.
     *
     * @param thisStart the start value of the first segment
     * @param thisEnd the end value of the first segment
     * @param otherStart the start value of the second segment
     * @param otherEnd the end value of the second segment
     * @return the overlap type between the two segments
     */
    public static OverlapType computeOverlap(long thisStart, long thisEnd, long otherStart, long otherEnd) {
        if ((thisEnd <= otherStart) || (thisStart >= otherEnd)) {
            return OverlapType.NO_OVERLAP;
        } else if ((thisStart < otherStart) && (thisEnd < otherEnd)) {
            return OverlapType.PARTIAL_OVERLAP_START;
        } else if ((thisStart > otherStart) && (thisEnd > otherEnd)) {
            return OverlapType.PARTIAL_OVERLAP_END;
        } else if ((thisStart <= otherStart) && (thisEnd >= otherEnd)) {
            return OverlapType.FULLY_CONTAINS;
        } else if ((thisStart >= otherStart) && (thisEnd <= otherEnd)) {
            return OverlapType.IS_FULLY_CONTAINED;
        } else {
            return OverlapType.NO_OVERLAP;
        }
    }

}

