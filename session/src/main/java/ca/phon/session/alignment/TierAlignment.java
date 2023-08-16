package ca.phon.session.alignment;

import ca.phon.session.Tier;
import ca.phon.util.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent alignment between two tiers
 *
 */
public class TierAlignment {

    private final Tier<?> topTier;

    private final Tier<?> bottomTier;

    private final List<Tuple<?, ?>> alignedElements;

    private final List<Tuple<String, String>> alignedSubTypes;

    public TierAlignment(Tier<?> topTier, Tier<?> bottomTier, List<Tuple<?, ?>> alignedElements) {
        this(topTier, bottomTier, alignedElements, null);
    }

    public TierAlignment(Tier<?> topTier, Tier<?> bottomTier, List<Tuple<?, ?>> alignedElements, List<Tuple<String, String>> alignedSubTypes) {
        super();
        this.topTier = topTier;
        this.bottomTier = bottomTier;
        this.alignedElements = alignedElements;
        this.alignedSubTypes = alignedSubTypes;
    }

    public int length() {
        return this.alignedElements.size();
    }

    public Tier<?> getTopTier() {
        return topTier;
    }

    public Tier<?> getBottomTier() {
        return bottomTier;
    }

    public List<Tuple<?, ?>> getAlignedElements() {
        return Collections.unmodifiableList(this.alignedElements);
    }

    public boolean hasAlignedSubTypes() {
        return this.alignedSubTypes != null && !this.alignedSubTypes.isEmpty();
    }

    public List<Tuple<String, String>> getAlignedSubTypes() {
        return this.alignedSubTypes;
    }

}
