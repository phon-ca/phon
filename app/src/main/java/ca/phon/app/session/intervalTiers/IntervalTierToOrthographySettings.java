package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.TerminatorType;

/**
 * Settings for converting an interval tier to an orthography tier.
 *
 * @param intervalTierName the name of the interval tier to import from
 * @param importWorTier whether to import into a word tier as well
 * @param addTerminator whether to add a terminator at the end
 * @param terminatorType the type of terminator to add
 */
public record IntervalTierToOrthographySettings(String intervalTierName,
                                                boolean importWorTier,
                                                boolean addTerminator,
                                                TerminatorType terminatorType) {

    public IntervalTierToOrthographySettings(String intervalTierName,
                                            boolean importWorTier,
                                            boolean addTerminator,
                                            String terminator) {
        this(intervalTierName, importWorTier, addTerminator, TerminatorType.fromString(terminator));
    }

}
