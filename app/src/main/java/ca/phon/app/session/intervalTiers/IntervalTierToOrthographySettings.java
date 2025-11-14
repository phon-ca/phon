package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.TerminatorType;
import ca.phon.session.filter.RecordFilter;

/**
 * Settings for converting an interval tier to an orthography tier.
 *
 * @param intervalTierName the name of the interval tier to import from
 * @param importWorTier whether to import into a word tier as well
 * @param addTerminator whether to add a terminator at the end
 * @param terminatorType the type of terminator to add
 * @param recordFilter filter to apply to records (optional)
 */
public record IntervalTierToOrthographySettings(String intervalTierName,
                                                boolean importWorTier,
                                                boolean addTerminator,
                                                TerminatorType terminatorType,
                                                RecordFilter recordFilter) {

    public IntervalTierToOrthographySettings(String intervalTierName,
                                            boolean importWorTier,
                                            boolean addTerminator,
                                            String terminator) {
        this(intervalTierName, importWorTier, addTerminator, TerminatorType.fromString(terminator), null);
    }
    
    /**
     * Constructor without recordFilter for backward compatibility.
     */
    public IntervalTierToOrthographySettings(String intervalTierName,
                                            boolean importWorTier,
                                            boolean addTerminator,
                                            TerminatorType terminatorType) {
        this(intervalTierName, importWorTier, addTerminator, terminatorType, null);
    }

}
