package ca.phon.app.session.intervalTiers;

/**
 * Settings for converting an interval tier to a user-defined tier.
 *
 * @param intervalTierName the name of the interval tier to import from
 * @param recordTierName the name of the user-defined tier to create/populate
 * @param includeIntervalText whether to include the interval text in the user-defined tier
 */
public record IntervalTierToUserTierSettings(String intervalTierName,
                                             String recordTierName,
                                             boolean includeIntervalText) {
}
