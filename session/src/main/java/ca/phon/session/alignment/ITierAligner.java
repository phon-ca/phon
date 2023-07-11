package ca.phon.session.alignment;

import ca.phon.session.Tier;

/**
 * Interface for calculating alignment between two tiers
 *
 * @param <T> type of top tier
 * @param <S> type of aligned element in top tier
 * @param <B> type of bottom tier
 * @param <C> type of aligned element in bottom tier
 */
public interface ITierAligner<T, S, B, C> {

    public TierAlignment<T, S, B, C> calculateAlignment(Tier<T> topTier, Tier<B> bottomTier);

}
