package ca.phon.app.session.intervalTiers;

import ca.phon.session.filter.RecordFilter;

/**
 * Settings for converting an interval tier to phone intervals.
 *
 * @param intervalTierName name of the interval tier to import from
 * @param transliterationScheme transliteration scheme to convert text to phones
 * @param fontConversionScheme font conversion scheme to convert text from one IPA font to another (optional)
 * @param recordFilter filter to apply to records (optional)
 */
public record IntervalTierToPhoneIntervalsSettings(String intervalTierName,
                                                   String transliterationScheme,
                                                   String fontConversionScheme,
                                                   RecordFilter recordFilter) {
    
    /**
     * Constructor without recordFilter for backward compatibility.
     */
    public IntervalTierToPhoneIntervalsSettings(String intervalTierName,
                                               String transliterationScheme,
                                               String fontConversionScheme) {
        this(intervalTierName, transliterationScheme, fontConversionScheme, null);
    }
}
