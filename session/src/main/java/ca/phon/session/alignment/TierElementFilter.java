package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.usertier.UserTierData;

import java.util.List;

public interface TierElementFilter {

    public static TierElementFilter defaultElementFilter(Tier<?> tier) {
        if(tier.getDeclaredType() == Orthography.class) {
            return defaultOrthographyElementFilter();
        } else if(tier.getDeclaredType() == IPATranscript.class || tier.getDeclaredType() == PhoneAlignment.class) {
            return defaultIPAElementFilter();
        } else if(tier.getDeclaredType() == UserTierData.class) {
            return defaultUserTierElementFilter();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static OrthographyTierElementFilter defaultOrthographyElementFilter() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
            List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Pause);
        return new OrthographyTierElementFilter(alignableTypes, true, true, true, false, false);
    }

    public static IPATierElementFilter defaultIPAElementFilter() {
        final List<IPATierElementFilter.AlignableType> alignableTypes =
                List.of(IPATierElementFilter.AlignableType.Word, IPATierElementFilter.AlignableType.Pause);
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
