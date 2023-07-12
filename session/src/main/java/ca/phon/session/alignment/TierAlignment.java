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

    private final List<Tuple<String, String>> alignedSubTypes;

    public TierAlignment(Tier<T> topTier, Tier<B> bottomTier, List<Tuple<S, C>> alignedElements) {
        this(topTier, bottomTier, alignedElements, null);
    }

    public TierAlignment(Tier<T> topTier, Tier<B> bottomTier, List<Tuple<S, C>> alignedElements, List<Tuple<String, String>> alignedSubTypes) {
        super();
        this.topTier = topTier;
        this.bottomTier = bottomTier;
        this.alignedElements = alignedElements;
        this.alignedSubTypes = alignedSubTypes;
    }

    public int length() {
        return this.alignedElements.size();
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

    public boolean hasAlignedSubTypes() {
        return this.alignedSubTypes != null && this.alignedSubTypes.size() > 0;
    }

    public List<Tuple<String, String>> getAlignedSubTypes() {
        return this.alignedSubTypes;
    }

}
