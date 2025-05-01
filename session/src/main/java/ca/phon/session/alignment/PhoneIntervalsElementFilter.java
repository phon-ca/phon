package ca.phon.session.alignment;

import ca.phon.session.Tier;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.session.tierdata.TierString;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter for phone interval tiers.  This filter is used to
 * collect elements for cross-tier alignment.
 */
public class PhoneIntervalsElementFilter extends VisitorAdapter<TierElement> implements TierElementFilter {

    private List<TierData> tierDataList;

    private List<TierElement> currentTierElements;

    public PhoneIntervalsElementFilter() {
        super();
        reset();
    }

    private void reset() {
        this.tierDataList = new ArrayList<>();
        this.currentTierElements = new ArrayList<>();
    }

    @Visits
    public void visitString(TierString tierString) {
        if(!"/".equals(tierString.toString())) {
            this.currentTierElements.add(tierString);
        } else {
            if(!this.currentTierElements.isEmpty()) {
                final TierData tierData = new TierData(this.currentTierElements);
                this.tierDataList.add(tierData);
                this.currentTierElements = new ArrayList<>();
            }
        }
    }

    @Visits
    public void visitInternalMedia(TierInternalMedia internalMedia) {
        this.currentTierElements.add(internalMedia);
    }

    @Override
    public List<?> filterTier(Tier<?> tier) {
        reset();
        if(tier.getDeclaredType() != TierData.class) return List.of();
        final TierData tierData = (TierData) tier.getValue();
        if(tierData == null) return List.of();
        tierData.accept(this);
        final List<TierData> result = new ArrayList<>(this.tierDataList);
        return result;
    }

    @Override
    public void fallbackVisit(TierElement obj) {

    }

}
