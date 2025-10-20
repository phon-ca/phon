package ca.phon.app.session.intervalTiers;

/**
 * Settings for converting an interval tier to phone intervals.
 *
 * @param intervalTierName name of the interval tier to import from
 * @param transliterationScheme transliteration scheme to convert text to phones
 */
public record IntervalTierToPhoneIntervalsSettings(String intervalTierName,
                                                   String transliterationScheme) {
}
