package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.TerminatorType;

/**
 * Settings for converting an interval tier to word intervals.
 *
 * @param intervalTierName the name of the interval tier to import from
 * @param addTerminator whether to add a terminator at the end
 * @param terminatorType the type of terminator to add
 */
public record IntervalTierToWordIntervalsSettings(String intervalTierName,
                                                  boolean addTerminator,
                                                  TerminatorType terminatorType) {

    public IntervalTierToWordIntervalsSettings(String intervalTierName,
                                              boolean addTerminator,
                                              String terminator) {
        this(intervalTierName, addTerminator, TerminatorType.fromString(terminator));
    }

}

