package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.Quotation;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.tierdata.TierData;

import java.util.List;

public interface TierElementFilter {

    public static TierElementFilter defaultElementFilterForAlignedTypes(Class<?> tierType, Class<?> alignedType) {
        if(tierType == Orthography.class) {
            if(alignedType == Orthography.class) {
                return orthographyFilterForOrthographyAlignment();
            } else if(alignedType == IPATranscript.class || alignedType == PhoneAlignment.class) {
                return orthographyFilterForIPAAlignment();
            } else if(alignedType == TierData.class) {
                return orthographyFilterForUserTierAlignment();
            } else if(alignedType == MorTierData.class) {
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
            } else if(alignedType == MorTierData.class) {
                return ipaFilterForUserTierAlignment();
            } else {
                throw new IllegalArgumentException("Invalid aligned tier type " + alignedType);
            }
        } else if(tierType == TierData.class) {
            return defaultUserTierElementFilter();
        } else if(tierType == MorTierData.class) {
            return defaultMorTierElementFilter();
        } else {
            throw new IllegalArgumentException("Invalid tier type " + tierType);
        }
    }

    public static OrthographyTierElementFilter orthographyFilterForOrthographyAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
                List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Pause, OrthographyTierElementFilter.AlignableType.Terminator);
        final OrthographyTierElementFilter.Options options = new OrthographyTierElementFilter.Options(
                true, true, true, false,  false,false, false, false);
        return new OrthographyTierElementFilter(alignableTypes, options);
    }

    public static OrthographyTierElementFilter orthographyFilterForIPAAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
            List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Pause);
        final OrthographyTierElementFilter.Options options = new OrthographyTierElementFilter.Options(
                true, true, true, false, false, false, false, false);
        return new OrthographyTierElementFilter(alignableTypes, options);
    }

    public static OrthographyTierElementFilter orthographyFilterForUserTierAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
                List.of(OrthographyTierElementFilter.AlignableType.Word);
        final OrthographyTierElementFilter.Options options = new OrthographyTierElementFilter.Options(
                true, true, true, false, false, false, false, false);
        return new OrthographyTierElementFilter(alignableTypes, options);
    }

    public static OrthographyTierElementFilter orthographyFilterForMorTierAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
                List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Quotation,
                        OrthographyTierElementFilter.AlignableType.TagMarker, OrthographyTierElementFilter.AlignableType.Terminator);
        final OrthographyTierElementFilter.Options options = new OrthographyTierElementFilter.Options(
                true, true, true, true, true, false, false, false);
        return new OrthographyTierElementFilter(alignableTypes, options);
    }

    public static IPATierElementFilter ipaFilterForIPAAlignment() {
        final List<IPATierElementFilter.AlignableType> alignableTypes =
                List.of(IPATierElementFilter.AlignableType.Word, IPATierElementFilter.AlignableType.Pause);
        return new IPATierElementFilter(alignableTypes);
    }

    public static IPATierElementFilter ipaFilterForOOrthographyAlignment() {
        final List<IPATierElementFilter.AlignableType> alignableTypes =
                List.of(IPATierElementFilter.AlignableType.Word, IPATierElementFilter.AlignableType.Pause);
        return new IPATierElementFilter(alignableTypes);
    }

    public static IPATierElementFilter ipaFilterForUserTierAlignment() {
        final List<IPATierElementFilter.AlignableType> alignableTypes =
                List.of(IPATierElementFilter.AlignableType.Word);
        return new IPATierElementFilter(alignableTypes);
    }

    public static UserTierElementFilter defaultUserTierElementFilter() {
        final List<UserTierElementFilter.AlignableType> alignableTypes =
                List.of(UserTierElementFilter.AlignableType.Type);
        return new UserTierElementFilter(alignableTypes);
    }

    public static TierElementFilter defaultMorTierElementFilter() {
        return new TierElementFilter() {
            @Override
            public List<?> filterTier(Tier<?> tier) {
                return ((Tier<MorTierData>)tier).getValue().getMors();
            }
        };
    }

    /**
     * Filter tier elements for cross tier alignment
     *
     * @param tier
     * @return list of alignable elements in tier
     */
    public List<?> filterTier(Tier<?> tier);

}
