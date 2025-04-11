package ca.phon.app.session.timeline;

import ca.phon.orthography.InternalMedia;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Visitor for updating tier intervals. This visitor will create a new
 * tier data object with the provided intervals replacing the old ones.
 */
public class TierDataIntervalUpdateVisitor extends VisitorAdapter<TierElement> {

    private final List<TierElement> tierElements = new ArrayList<>();

    private final List<TierInternalMedia> updatedIntervals;

    private Iterator<TierInternalMedia> updatedIntervalsIter = null;

    public TierDataIntervalUpdateVisitor(List<TierInternalMedia> updatedIntervals) {
        super();
        this.updatedIntervals = updatedIntervals;
        reset();
    }

    public void reset() {
        tierElements.clear();
        updatedIntervalsIter = updatedIntervals.iterator();
    }

    @Visits
    public void visitInternalMedia(TierInternalMedia internalMedia) {
        if(updatedIntervalsIter.hasNext()) {
            TierInternalMedia newInterval = updatedIntervalsIter.next();
            tierElements.add(newInterval);
        } else {
            tierElements.add(internalMedia);
        }
    }

    public TierData getUpdatedTierData() {
        return new TierData(tierElements);
    }

    @Override
    public void fallbackVisit(TierElement obj) {
        tierElements.add(obj);
    }

}
