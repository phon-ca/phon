package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.alignment.aligners.OrthoToIPAAligner;
import ca.phon.session.alignment.aligners.OrthoToOrthoAligner;
import ca.phon.session.alignment.aligners.OrthoToPhoneAlignmentAligner;
import ca.phon.session.alignment.aligners.OrthoToUserTierAligner;
import ca.phon.session.usertier.UserTierData;
import ca.phon.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public final class TierAligner {

    /**
     * Return a list of left-aligned tuples
     *
     * @param topElements
     * @param bottomElements
     * @return list of aligned tuples
     * @param <S> type of top element
     * @param <C> type of bottom element
     */
    public static <S, C> List<Tuple<S, C>> mapAlignedElements(List<S> topElements, List<C> bottomElements) {
        final List<Tuple<S, C>> retVal = new ArrayList<>();
        final int n = Math.max(topElements.size(), bottomElements.size());
        for(int i = 0; i < n; i++) {
            final S topElement = i < topElements.size() ? topElements.get(i) : null;
            final C bottomElement = i < bottomElements.size() ? bottomElements.get(i) : null;
            final Tuple<S, C> tuple = new Tuple<>(topElement, bottomElement);
            retVal.add(tuple);
        }
        return retVal;
    }

    public static <T, S, B, C> TierAlignment<B, C, T, S> mirrorTierAlignment(TierAlignment<T, S, B, C> alignment) {
        return new TierAlignment<>(alignment.getBottomTier(), alignment.getTopTier(), mirrorAlignedElements(alignment.getAlignedElements()));
    }

    public static <S, C> List<Tuple<C, S>> mirrorAlignedElements(List<Tuple<S, C>> elements) {
        final List<Tuple<C, S>> retVal = new ArrayList<>();
        for(Tuple<S, C> item:elements) {
            retVal.add(new Tuple<>(item.getObj2(), item.getObj1()));
        }
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public static TierAlignment<?, ?, ?, ?> alignTiers(Tier<?> topTier, Tier<?> bottomTier) {
        boolean mirror = false;
        final Class<?> topType = topTier.getDeclaredType();
        final Class<?> bottomType = topType.getComponentType();
        // default tier alignment
        final Tuple<?, ?> tuple = new Tuple<>(topTier.getValue(), bottomTier.getValue());
        TierAlignment<?, ?, ?, ?> alignment = new TierAlignment<>(topTier, bottomTier, List.of(tuple));
        if(bottomTier.getTierAlignmentRules().getType() != TierAlignmentRules.TierAlignmentType.None) {
            if(topType == Orthography.class || bottomType == Orthography.class) {
                mirror = topType != Orthography.class;
                final Tier<Orthography> tier1 = !mirror ? (Tier<Orthography>) topTier : (Tier<Orthography>) bottomTier;
                final Class<?> otherType = mirror ? topType : bottomType;
                final Tier<?> tier2 = !mirror ? bottomTier : topTier;
                if(otherType == Orthography.class) {
                    var aligner = new OrthoToOrthoAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<Orthography>)tier2);
                } else if(otherType == IPATranscript.class) {
                    var aligner = new OrthoToIPAAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<IPATranscript>)tier2);
                } else if(otherType == PhoneAlignment.class) {
                    var aligner = new OrthoToPhoneAlignmentAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<PhoneAlignment>)tier2);
                } else if(otherType == UserTierData.class) {
                    var aligner = new OrthoToUserTierAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<UserTierData>)tier2);
                }
            } else if(topType == IPATranscript.class || bottomType == IPATranscript.class) {
                mirror = topType != IPATranscript.class;

            }
        }
        return mirror ? mirrorTierAlignment(alignment) : alignment;
    }

}
