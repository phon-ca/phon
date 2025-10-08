package ca.phon.app.session.intervalTiers;

import ca.phon.session.IntervalTier;
import ca.phon.session.tierdata.TierElement;
import ca.phon.session.tierdata.TierInternalMedia;
import ca.phon.session.tierdata.TierString;
import ca.phon.util.Range;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for producing a list of TimelineInterval objects from a
 * tier data object.  Words are appended to a buffer until an internal
 * media is encountered.  At that point, a new TimelineInterval is created
 * and added to the list.
 */
public class TierDataIntervalVisitor extends VisitorAdapter<TierElement> {

    private final StringBuilder buffer = new StringBuilder();

    private final List<IntervalTier.Interval> intervals = new ArrayList<>();

    /**
     * Word interval ranges (start,end)
     */
    private final List<Range> ranges = new ArrayList<>();

    private final List<String> ignoreWords = new ArrayList<>();

    public TierDataIntervalVisitor() {
        super();
    }

    public void addIgnoreWord(String word) {
        if(word != null && !word.isEmpty()) {
            ignoreWords.add(word);
        }
    }

    public void removeIgnoreWord(String word) {
        if(word != null && !word.isEmpty()) {
            ignoreWords.remove(word);
        }
    }

    public void reset() {
        buffer.setLength(0);
        intervals.clear();
    }

    @Visits
    public void visitTierWord(TierString tierWord) {
        if(ignoreWords.contains(tierWord.text())) return;
        if(buffer.length() > 0) buffer.append(" ");
        buffer.append(tierWord.text());
    }

    @Visits
    public void visitTierInternalMedia(TierInternalMedia tierInternalMedia) {
        if(buffer.length() > 0) {
            final String lbl = buffer.toString();

            final int startIdx = intervals.size();

            // create IntervalTier.Interval
            final IntervalTier.Interval interval =
                    new IntervalTier.Interval(tierInternalMedia.getStartTime(), tierInternalMedia.getEndTime(), lbl);
            intervals.add(interval);

            final int endIdx = intervals.size() - 1;
            ranges.add(new Range(startIdx, endIdx));

            // reset interval string
            buffer.setLength(0);
        }
    }

    /**
     * Get the list of intervals created by this visitor.
     *
     * @return
     */
    public List<IntervalTier.Interval> getIntervals() {
        return List.copyOf(intervals);
    }

    @Override
    public void fallbackVisit(TierElement obj) {

    }

}
