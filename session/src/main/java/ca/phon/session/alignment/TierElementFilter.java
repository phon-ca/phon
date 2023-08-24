package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.usertier.UserTierData;

import java.util.List;

public interface TierElementFilter {

    public static TierElementFilter defaultElementFilterForAlignedTypes(Class<?> tierType, Class<?> alignedType) {
        if(tierType == Orthography.class) {
            if(alignedType == Orthography.class) {
                return orthographyFilterForOrthographyAlignment();
            } else if(alignedType == IPATranscript.class || alignedType == PhoneAlignment.class) {
                return orthographyFilterForIPAAlignment();
            } else if(alignedType == UserTierData.class) {
                return orthographyFilterForUserTierAlignment();
            } else {
                throw new IllegalArgumentException("Invalid aligned tier type " + alignedType);
            }
        } else if(tierType == IPATranscript.class || tierType == PhoneAlignment.class) {
            if(alignedType == Orthography.class) {
                return ipaFilterForOOrthographyAlignment();
            } else if(alignedType == IPATranscript.class || alignedType == PhoneAlignment.class) {
                return ipaFilterForIPAAlignment();
            } else if(alignedType == UserTierData.class) {
                return ipaFilterForUserTierAlignment();
            } else {
                throw new IllegalArgumentException("Invalid aligned tier type " + alignedType);
            }
        } else if(tierType == UserTierData.class) {
            return defaultUserTierElementFilter();
        } else {
            throw new IllegalArgumentException("Invalid tier type " + tierType);
        }
    }

    public static OrthographyTierElementFilter orthographyFilterForOrthographyAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
                List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Pause, OrthographyTierElementFilter.AlignableType.Terminator);
        return new OrthographyTierElementFilter(alignableTypes, true, true, true, false, false);
    }

    public static OrthographyTierElementFilter orthographyFilterForIPAAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
            List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Pause);
        return new OrthographyTierElementFilter(alignableTypes, true, true, true, false, false);
    }

    public static OrthographyTierElementFilter orthographyFilterForUserTierAlignment() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
                List.of(OrthographyTierElementFilter.AlignableType.Word);
        return new OrthographyTierElementFilter(alignableTypes, true, true, true, false, false);
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

    /**
     * Filter tier elements for cross tier alignment
     *
     * @param tier
     * @return list of alignable elements in tier
     */
    public List<?> filterTier(Tier<?> tier);

}