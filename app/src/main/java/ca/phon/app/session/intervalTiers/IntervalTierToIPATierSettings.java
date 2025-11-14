package ca.phon.app.session.intervalTiers;

import ca.phon.session.filter.RecordFilter;

/**
 * Settings for converting an interval tier to an IPA tier.
 *
 * @param intervalTierName name of the interval tier to import from
 * @param recordTierName name of the IPA tier to create/populate
 * @param language language code for syllabification
 * @param transliterationScheme transliteration scheme to convert text to IPA
 * @param fontConversionScheme font conversion scheme to convert text from one IPA font to another (optional)
 * @param recordFilter filter to apply to records (optional)
 */
public record IntervalTierToIPATierSettings(String intervalTierName,
                                            String recordTierName,
                                            String language,
                                            String transliterationScheme,
                                            String fontConversionScheme,
                                            RecordFilter recordFilter) {
    
    /**
     * Constructor without recordFilter for backward compatibility.
     */
    public IntervalTierToIPATierSettings(String intervalTierName,
                                        String recordTierName,
                                        String language,
                                        String transliterationScheme,
                                        String fontConversionScheme) {
        this(intervalTierName, recordTierName, language, transliterationScheme, fontConversionScheme, null);
    }
}
