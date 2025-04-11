package ca.phon.app.session.timeline;

import ca.phon.orthography.*;
import ca.phon.session.TimelineTier;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.List;

/**
 * Visitor for producing a list of TimelineInterval objects from an
 * orthography object.  Words are appended to a buffer until an internal
 * media is encountered.  At that point, a new TimelineInterval is created
 * and added to the list.
 */
public class OrthoIntervalVisitor extends VisitorAdapter<OrthographyElement> {

    private final StringBuilder buffer = new StringBuilder();

    private final List<TimelineTier.Interval> intervals = new java.util.ArrayList<>();

    public void reset() {
        buffer.setLength(0);
        intervals.clear();
    }

    @Visits
    public void visitOrthoGroup(OrthoGroup group) {
        for(OrthographyElement element : group.getElements()) {
            super.visit(element);
        }
    }

    @Visits
    public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
        for(OrthographyElement element : phoneticGroup.getElements()) {
            super.visit(element);
        }
    }

    @Visits
    public void visitCompoundWord(CompoundWord compoundWord) {
        if(buffer.length() > 0) buffer.append(" ");
        buffer.append(compoundWord.text());
    }

    @Visits
    public void visitWord(Word word) {
        if(buffer.length() > 0) buffer.append(" ");
        buffer.append(word.text());
    }


    @Visits
    public void visitInternalMedia(InternalMedia internalMedia) {
        if(buffer.length() > 0) {
            final String lbl = buffer.toString();
            final TimelineTier.Interval interval =
                    new TimelineTier.Interval(internalMedia.getStartTime(), internalMedia.getEndTime(), lbl);
            intervals.add(interval);

            // reset interval string
            buffer.setLength(0);
        }
    }

    /**
     * Return list of intervals, the returned list is a copy of the
     * internal list.
     *
     * @return list of intervals
     */
    public List<TimelineTier.Interval> getIntervals() {
        return List.copyOf(intervals);
    }

    @Override
    public void fallbackVisit(OrthographyElement obj) {

    }

}
