package ca.phon.app.session.intervalTiers;

public record IntervalTierToUserTierSettings(String intervalTierName,
                                             String recordTierName,
                                             boolean includeIntervalText) {
}
