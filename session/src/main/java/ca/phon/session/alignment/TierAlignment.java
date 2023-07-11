package ca.phon.session.alignment;

import ca.phon.session.Tier;
import ca.phon.util.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent alignment between two tiers
 *
 * @param <T> top tier declared type
 * @param <S> aligned element type of top tier
 * @param <B> bottom tier declared type
 * @param <C> aligned element type of bottom tier
 */
public class TierAlignment<T, S, B, C> {

    private final Tier<T> topTier;

    private final Tier<B> bottomTier;

    private final List<Tuple<S, C>> alignedElements;

    public TierAlignment(Tier<T> topTier, Tier<B> bottomTier, List<Tuple<S, C>> alignedElements) {
        super();
        this.topTier = topTier;
        this.bottomTier = bottomTier;
        this.alignedElements = alignedElements;
    }

    public Tier<T> getTopTier() {
        return topTier;
    }

    public Tier<B> getBottomTier() {
        return bottomTier;
    }

    public List<Tuple<S, C>> getAlignedElements() {
        return Collections.unmodifiableList(this.alignedElements);
    }

}
