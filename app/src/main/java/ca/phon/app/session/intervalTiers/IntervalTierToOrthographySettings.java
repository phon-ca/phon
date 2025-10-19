package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.TerminatorType;

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
