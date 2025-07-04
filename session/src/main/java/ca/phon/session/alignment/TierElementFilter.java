package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.Mor;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.orthography.mor.MorphemicBaseType;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.UserTierType;
import ca.phon.session.tierdata.TierData;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter for tier elements for cross tier alignment
 */
public interface TierElementFilter {

    /**
     * Create default element filter for given tiers
     *
     * @param tier1 the top tier
     * @param tier2 the aligned tier
     * @return default element filter for given tiers
     */
    public static TierElementFilter filterForAlignedTiers(Tier<?> tier1, Tier<?> tier2) {
        TierElementFilter retVal = defaultElementFilterForAlignedTypes(tier1.getDeclaredType(), tier2.getDeclaredType());

        if(UserTierType.Wor.getPhonTierName().equals(tier1.getName()) && tier1.getDeclaredType().equals(Orthography.class)) {
            retVal = worElementFilter();
        } else if(UserTierType.PhoneIntervals.getPhonTierName().equals(tier1.getName()) && tier1.getDeclaredType().equals(TierData.class)) {
            // todo
        }

        return retVal;
    }

    /**
     * Create default element filter for given tier types
     *
     * @param tierType the top tier type
     * @param alignedType the aligned tier type
     * @return default element filter for given tier types
     */
    public static TierElementFilter defaultElementFilterForAlignedTypes(Class<?> tierType, Class<?> alignedType) {
        if(tierType == Orthography.class) {
            if(alignedType == Orthography.class) {
                return orthographyFilterForOrthographyAlignment();
            } else if(alignedType == IPATranscript.class || alignedType == PhoneAlignment.class) {
                return orthographyFilterForIPAAlignment();
            } else if(alignedType == TierData.class) {
                return orthographyFilterForUserTierAlignment();
            } else if(alignedType == MorTierData.class || alignedType == GraspTierData.class) {
                return orthographyFilterForMorTierAlignment();
            } else {
                throw new IllegalArgumentException("Invalid aligned tier type " + alignedType);
            }
        } else if(tierType == IPATranscript.class || tierType == PhoneAlignment.class) {
            if(alignedType == Orthography.class) {
                return ipaFilterForOOrthographyAlignment();
            } else if(alignedType == IPATranscript.class || alignedType == PhoneAlignment.class) {
                return ipaFilterForIPAAlignment();
            } else if(alignedType == TierData.class) {
                return ipaFilterForUserTierAlignment();
            } else if(alignedType == MorTierData.class || alignedType == GraspTierData.class) {
                return ipaFilterForUserTierAlignment();
            } else {
                throw new IllegalArgumentException("Invalid aligned tier type " + alignedType);
            }
        } else if(tierType == TierData.class) {
            return defaultUserTierElementFilter();
        } else if(tierType == MorTierData.class) {
            if(alignedType == GraspTierData.class) {
                return morFilterForGraspTierAlignment();
            } else {
                return defaultMorTierElementFilter();
            }
        } else if(tierType == GraspTierData.class) {
            return defaultGraTierElementFilter();
        } else {
            throw new IllegalArgumentException("Invalid tier type " + tierType);
        }
    }

    /**
     * Orthography filter for alignment with another orthography tier
     * @return orthography filter for alignment with another orthography tier
     */
    public static OrthographyTierElementFilter orthographyFilterForOrthographyAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
                List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Pause, OrthographyTierElementFilter.AlignableType.Terminator);
        final OrthographyTierElementFilter.Options options = new OrthographyTierElementFilter.Options(
                true, true, true,
                false, true, true, true,
                false,false, false, false);
        return new OrthographyTierElementFilter(alignableTypes, options);
    }

    /**
     * Orthography filter for alignment with IPA tier
     * @return orthography filter for alignment with IPA tier
     */
    public static OrthographyTierElementFilter orthographyFilterForIPAAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
            List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Pause,
                    OrthographyTierElementFilter.AlignableType.PhoneticGroup);
        final OrthographyTierElementFilter.Options options = new OrthographyTierElementFilter.Options(
                true, true, true,
                false, true, true, true,
                false, false, true, true);
        return new OrthographyTierElementFilter(alignableTypes, options);
    }

    /**
     * Orthography filter for alignment with user defined tier
     * @return orthography filter for alignment with user defined tier
     */
    public static OrthographyTierElementFilter orthographyFilterForUserTierAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
                List.of(OrthographyTierElementFilter.AlignableType.Word);
        final OrthographyTierElementFilter.Options options = new OrthographyTierElementFilter.Options(
                true, true, true,
                false, true, true, true,
                false, true, true, true);
        return new OrthographyTierElementFilter(alignableTypes, options);
    }

    /**
     * Orthography filter for alignment with morpheme tier
     * @return orthography filter for alignment with morpheme tier
     */
    public static OrthographyTierElementFilter orthographyFilterForMorTierAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
                List.of(OrthographyTierElementFilter.AlignableType.Word,
                        OrthographyTierElementFilter.AlignableType.TagMarker, OrthographyTierElementFilter.AlignableType.Terminator);
        final OrthographyTierElementFilter.Options options = new OrthographyTierElementFilter.Options(
                false, true, true,
                true, false, false, false,
                true, false, false, true);
        return new OrthographyTierElementFilter(alignableTypes, options);
    }

    /**
     * IPA filter for alignment with another IPA tier
     * @return IPA filter for alignment with another IPA tier
     */
    public static IPATierElementFilter ipaFilterForIPAAlignment() {
        final List<IPATierElementFilter.AlignableType> alignableTypes =
                List.of(IPATierElementFilter.AlignableType.Word, IPATierElementFilter.AlignableType.Pause);
        return new IPATierElementFilter(alignableTypes);
    }

    /**
     * IPA filter for alignment with orthography tier
     * @return IPA filter for alignment with orthography tier
     */
    public static IPATierElementFilter ipaFilterForOOrthographyAlignment() {
        final List<IPATierElementFilter.AlignableType> alignableTypes =
                List.of(IPATierElementFilter.AlignableType.Word, IPATierElementFilter.AlignableType.Pause);
        return new IPATierElementFilter(alignableTypes);
    }

    /**
     * IPA filter for alignment with user defined tier
     * @return IPA filter for alignment with user defined tier
     */
    public static IPATierElementFilter ipaFilterForUserTierAlignment() {
        final List<IPATierElementFilter.AlignableType> alignableTypes =
                List.of(IPATierElementFilter.AlignableType.Word);
        return new IPATierElementFilter(alignableTypes);
    }

    /**
     * IPA filter for alignment with morpheme tier
     * @return IPA filter for alignment with morpheme tier
     */
    public static UserTierElementFilter defaultUserTierElementFilter() {
        final List<UserTierElementFilter.AlignableType> alignableTypes =
                List.of(UserTierElementFilter.AlignableType.Type);
        return new UserTierElementFilter(alignableTypes);
    }

    /**
     * Morpheme filter for alignment with morpheme tier
     * @return morpheme filter for alignment with morpheme tier
     */
    public static TierElementFilter defaultMorTierElementFilter() {
        return new TierElementFilter() {
            @Override
            public List<?> filterTier(Tier<?> tier) {
                return ((Tier<MorTierData>)tier).getValue().getMors();
            }
        };
    }

    /**
     * Morpheme filter for alignment with grasp tier.  Grasp tiers are aligned
     * with the morpheme tier, which in turn is aligned with the main line
     * @return morpheme filter for alignment with grasp tier
     */
    public static TierElementFilter morFilterForGraspTierAlignment() {
        return new TierElementFilter() {
            @Override
            public List<?> filterTier(Tier<?> tier) {
                List<MorphemicBaseType> retVal = new ArrayList<>();
                final MorTierData morTierData = (MorTierData) tier.getValue();
                for(Mor mor:morTierData) {
                    mor.getMorPres().forEach(retVal::add);
                    retVal.add(mor);
                    mor.getMorPosts().forEach(retVal::add);
                }
                return retVal;
            }
        };
    }

    /**
     * Grasp filter for alignment with morpheme tier
     * @return grasp filter for alignment with morpheme tier
     */
    public static TierElementFilter defaultGraTierElementFilter() {
        return new TierElementFilter() {
            @Override
            public List<?> filterTier(Tier<?> tier) {
                return ((Tier<GraspTierData>)tier).getValue().getGrasps();
            }
        };
    }

    /**
     * Word and interval filter for the word intervals tier.  This filter
     * will return a list of Orthography objects which are slices of
     * the original Orthography object.  The slices are the words and their
     * associated intervals.
     *
     * @return word intervals filter
     */
    public static TierElementFilter worElementFilter() {
        return new WorElementFilter();
    }

    /**
     * Filter for the phone intervals tier.  This filter will return a list
     * of TierData objects which are slices of the original TierData object.
     * The slices are the phones for a single word and their associated intervals.
     * There should be one slice for each alignable element in the IPA Actual tier.
     *
     * @return phone intervals filter
     */
    public static TierElementFilter phoneIntervalsElementFilter() {
        return new PhoneIntervalsElementFilter();
    }

    /**
     * Filter tier elements for cross tier alignment
     *
     * @param tier
     * @return list of alignable elements in tier
     */
    public List<?> filterTier(Tier<?> tier);

}
