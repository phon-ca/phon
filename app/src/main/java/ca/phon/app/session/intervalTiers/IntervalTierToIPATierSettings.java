package ca.phon.app.session.intervalTiers;

/**
 * Settings for converting an interval tier to an IPA tier.
 *
 * @param intervalTierName name of the interval tier to import from
 * @param recordTierName name of the IPA tier to create/populate
 * @param language language code for syllabification
 * @param transliterationScheme transliteration scheme to convert text to IPA
 * @param fontConversionScheme font conversion scheme to convert text from one IPA font to another (optional)
 */
public record IntervalTierToIPATierSettings(String intervalTierName,
                                            String recordTierName,
                                            String language,
                                            String transliterationScheme,
                                            String fontConversionScheme) {
}
