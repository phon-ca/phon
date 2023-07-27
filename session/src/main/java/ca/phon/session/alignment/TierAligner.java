package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.alignment.aligners.*;
import ca.phon.session.usertier.UserTierData;
import ca.phon.util.Tuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        final Class<?> bottomType = bottomTier.getDeclaredType();
        // default tier alignment
        final Tuple<?, ?> tuple = new Tuple<>(topTier.getValue(), bottomTier.getValue());
        TierAlignment<?, ?, ?, ?> alignment = new TierAlignment<>(topTier, bottomTier, List.of(tuple));
        final TierAlignmentRules alignmentRules = bottomTier.getTierAlignmentRules();
        if(alignmentRules.getType() != TierAlignmentRules.TierAlignmentType.None) {
            if(topType == Orthography.class || bottomType == Orthography.class) {
                mirror = topType != Orthography.class;
                final Tier<Orthography> tier1 = !mirror ? (Tier<Orthography>) topTier : (Tier<Orthography>) bottomTier;
                final Class<?> otherType = mirror ? topType : bottomType;
                final Tier<?> tier2 = !mirror ? bottomTier : topTier;
                if(otherType == Orthography.class) {
                    var aligner = new OrthoToOrthoAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<Orthography>)tier2, alignmentRules);
                } else if(otherType == IPATranscript.class) {
                    var aligner = new OrthoToIPAAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<IPATranscript>)tier2, alignmentRules);
                } else if(otherType == PhoneAlignment.class) {
                    var aligner = new OrthoToPhoneAlignmentAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<PhoneAlignment>)tier2, alignmentRules);
                } else if(otherType == UserTierData.class) {
                    var aligner = new OrthoToUserTierAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<UserTierData>)tier2, alignmentRules);
                }
            } else if(topType == IPATranscript.class || bottomType == IPATranscript.class) {
                mirror = topType != IPATranscript.class;
                final Tier<IPATranscript> tier1 = !mirror ? (Tier<IPATranscript>)topTier : (Tier<IPATranscript>)bottomTier;
                final Tier<?> tier2 = !mirror ? bottomTier : topTier;
                final Class<?> otherType = mirror ? topType : bottomType;
                if(otherType == IPATranscript.class) {
                    var aligner = new IPAtoIPAAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<IPATranscript>) tier2, alignmentRules);
                } else if(otherType == PhoneAlignment.class) {
                    var aligner = new IPAToPhoneAlignmentAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<PhoneAlignment>) tier2, alignmentRules);
                } else if(otherType == UserTierData.class) {
                    var aligner = new IPAToUserTierAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<UserTierData>)tier2, alignmentRules);
                }
            } else if(topType == PhoneAlignment.class || bottomType == PhoneAlignment.class) {
                mirror = topType != PhoneAlignment.class;
                final Tier<PhoneAlignment> tier1 = !mirror ? (Tier<PhoneAlignment>) topTier : (Tier<PhoneAlignment>) bottomTier;
                final Tier<?> tier2 = !mirror ? bottomTier : topTier;
                final Class<?> otherType = mirror ? topType : bottomType;
                if(otherType == PhoneAlignment.class) {
                    var aligner = new PhoneAlignmentToPhoneAlignmentAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<PhoneAlignment>) tier2, alignmentRules);
                } else if(otherType == UserTierData.class) {
                    var aligner = new PhoneAlignmentToUserTierAligner();
                    alignment = aligner.calculateAlignment(tier1, (Tier<UserTierData>) tier2, alignmentRules);
                }
            } else if(topType == UserTierData.class && bottomType == UserTierData.class) {
                var aligner = new UserTierToUserTierAligner();
                alignment = aligner.calculateAlignment((Tier<UserTierData>)topTier, (Tier<UserTierData>) bottomTier, alignmentRules);
            }
        }
        return mirror ? mirrorTierAlignment(alignment) : alignment;
    }

    /**
     * Calculate cross tier alignment for all tiers which align by type
     *
     * @param record
     * @param topTier
     *
     * @return cross tier alignment for record
     */
    public static CrossTierAlignment calculateCrossTierAlignment(Record record, Tier<?> topTier) {
        Map<String, TierAlignment<?,?,?,?>> alignmentMap = new LinkedHashMap<>();
        alignmentMap.put(SystemTierType.Orthography.getName(),
                TierAligner.alignTiers(topTier, record.getOrthographyTier()));
        alignmentMap.put(SystemTierType.IPATarget.getName(),
                TierAligner.alignTiers(topTier, record.getIPATargetTier()));
        alignmentMap.put(SystemTierType.IPAActual.getName(),
                TierAligner.alignTiers(topTier, record.getIPAActualTier()));
        alignmentMap.put(SystemTierType.PhoneAlignment.getName(),
                TierAligner.alignTiers(topTier, record.getPhoneAlignmentTier()));
        for(String tierName:record.getUserDefinedTierNames()) {
            final Tier<?> bottomTier = record.getTier(tierName);
            if(bottomTier != null && bottomTier.getTierAlignmentRules().getType() != TierAlignmentRules.TierAlignmentType.None) {
                alignmentMap.put(tierName, TierAligner.alignTiers(topTier, bottomTier));
            }
        }
        return new CrossTierAlignment(topTier, alignmentMap);
    }

}
