package ca.phon.app.session.intervalTiers;

import ca.phon.session.Participant;
import ca.phon.session.filter.RecordFilter;

public record IntervalTierToRecordSegmentsSettings(String intervalTierName,
                                                   boolean groupContiguousIntervals,
                                                   float maxGapLength,
                                                   float padding,
                                                   Participant speaker,
                                                   boolean overwriteExistingRecords,
                                                   RecordFilter recordFilter) {
    
    /**
     * Constructor without recordFilter for backward compatibility.
     */
    public IntervalTierToRecordSegmentsSettings(String intervalTierName,
                                               boolean groupContiguousIntervals,
                                               float maxGapLength,
                                               float padding,
                                               Participant speaker,
                                               boolean overwriteExistingRecords) {
        this(intervalTierName, groupContiguousIntervals, maxGapLength, padding, speaker, overwriteExistingRecords, null);
    }

}
