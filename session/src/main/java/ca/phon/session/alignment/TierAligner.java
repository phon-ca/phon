package ca.phon.session.alignment;

import ca.phon.session.Tier;

/**
 * Determine alignment between two tiers.
 */
public interface TierAligner<T, S, B, C> {

    public TierAlignment<T, S, B, C> calculateAlignment(Tier<T> topTier, Tier<B> bottomTier);

}
