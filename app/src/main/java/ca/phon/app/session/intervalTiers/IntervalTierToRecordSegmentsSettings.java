package ca.phon.app.session.intervalTiers;

import ca.phon.session.Participant;

public record IntervalTierToRecordSegmentsSettings(String intervalTierName,
                                                   boolean groupContiguousIntervals,
                                                   float maxGapLength,
                                                   float padding,
                                                   Participant speaker,
                                                   boolean overwriteExistingRecords) {

}
