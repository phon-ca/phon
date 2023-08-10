package ca.phon.session.alignment.aligners;

import ca.phon.session.Tier;
import ca.phon.session.alignment.OrthographyTierElementFilter;

import java.util.List;

public interface TierElementFilter {

    public static OrthographyTierElementFilter defaultOrthographyElementFilter() {
        final List<OrthographyTierElementFilter.AlignableType> alignableTypes =
            List.of(OrthographyTierElementFilter.AlignableType.Word, OrthographyTierElementFilter.AlignableType.Pause);
        return new OrthographyTierElementFilter(alignableTypes, true, true, true, false, false)
    }



    /**
     * Filter tier elements for cross tier alignment
     *
     * @param tier
     * @return list of alignable elements in tier
     */
    public List<?> filterTier(Tier<?> tier);

}
