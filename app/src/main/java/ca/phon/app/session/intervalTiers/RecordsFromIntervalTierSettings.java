package ca.phon.app.session.intervalTiers;

import ca.phon.session.Participant;

public record RecordsFromIntervalTierSettings(String intervalTierName,
                                              boolean groupContiguousIntervals,
                                              float maxGapLength,
                                              float padding,
                                              Participant speaker) {

}
