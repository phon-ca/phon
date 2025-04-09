package ca.phon.app.session.timeline;

import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.spi.TimelineTierSPI;
import ca.phon.session.tierdata.TierData;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.session.tierdata.TierString;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for record tier data so it may be viewed as an interval tier.
 * The record tier should be of type {@link ca.phon.session.tierdata.TierData}
 * with intervals/points placed at the end of label data.
 *
 * e.g., hello •0.-999-1.28• world •1.28-2.08•
 */
public class RecordTimelineTier implements TimelineTierSPI {

    private final Session session;

    private final String tierName;

    public RecordTimelineTier(Session session, String tierName) {
        this.session = session;
        this.tierName = tierName;
    }

    @Override
    public String getName() {
        return tierName;
    }

    public List<TimelineTier.Interval> getIntervals(Record record) {
        // process tier data
        final Tier<TierData> tier = record.getTier(this.tierName, TierData.class);
        if(tier == null) return List.of();

        final TierData tierData = tier.getValue();
        if(tierData == null) return List.of();

        final List<TimelineTier.Interval> intervals = new ArrayList<>();
        final StringBuilder intervalStr = new StringBuilder();
        for(int i = 0; i < tierData.size(); i++) {
            final TierElement element = tierData.elementAt(i);
            if(element instanceof TierInternalMedia internalMedia) {
                final String lbl = intervalStr.toString();

                // create TimelineTier.Inverval
                final TimelineTier.Interval interval =
                        new TimelineTier.Interval(internalMedia.getStartTime(), internalMedia.getEndTime(), lbl);
                intervals.add(interval);

                // reset interval string
                intervalStr.setLength(0);
            } else if(element instanceof TierString tierString) {
                if(intervalStr.length() > 0) {
                    intervalStr.append(" ");
                }
                intervalStr.append(tierString.text());
            }
        }
        return intervals;
    }

    @Override
    public List<TimelineTier.Interval> getIntervals() {
        final List<TimelineTier.Interval> intervals = new ArrayList<>();
        for(var record:session.getRecords()) {
            intervals.addAll(getIntervals(record));
        }
        return intervals;
    }

    // not implemented
    @Override
    public boolean addInterval(TimelineTier.Interval interval, TimelineTier.InsertionStrategy insertionStrategy) {
        return false;
    }

    // not implemented
    @Override
    public boolean removeInterval(TimelineTier.Interval interval) {
        return false;
    }
}
