package ca.phon.app.session.timeline;

import ca.phon.orthography.*;
import ca.phon.session.TimelineTier;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Visitor for updating orthography intervals.  This visitor will create a new orthography
 * object with the provided intervals replacing the old ones.
 */
public class OrthoIntervalUpdateVisitor extends VisitorAdapter<OrthographyElement> {

    private Stack<OrthographyBuilder> builderStack = new Stack<>();

    private final List<TimelineTier.Interval> updatedIntervals;

    private Iterator<TimelineTier.Interval> updatedIntervalsIter = null;

    public OrthoIntervalUpdateVisitor(List<TimelineTier.Interval> updatedIntervals) {
        super();
        this.updatedIntervals = updatedIntervals;
        reset();
    }

    public void reset() {
        builderStack.clear();
        builderStack.push(new OrthographyBuilder());
        updatedIntervalsIter = updatedIntervals.iterator();
    }

    @Visits
    public void visitInternalMedia(InternalMedia internalMedia) {
        if(updatedIntervalsIter.hasNext()) {
            TimelineTier.Interval newInterval = updatedIntervalsIter.next();
            final InternalMedia newInternalMedia =
                    new InternalMedia(newInterval.getStart(), newInterval.getEnd());
            builderStack.peek().append(newInternalMedia);
        } else {
            builderStack.peek().append(internalMedia);
        }
    }

    @Visits
    public void visitOrthoGroup(OrthoGroup group) {
        builderStack.push(new OrthographyBuilder());
        for(OrthographyElement element : group.getElements()) {
            super.visit(element);
        }
        OrthographyBuilder builder = builderStack.pop();
        if(builder.size() > 0) {
            final OrthoGroup orthoGroup = new OrthoGroup(builder.getElements(), group.getAnnotations());
            builderStack.peek().append(orthoGroup);
        }
    }

    @Visits
    public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
        builderStack.push(new OrthographyBuilder());
        for(OrthographyElement element : phoneticGroup.getElements()) {
            super.visit(element);
        }
        OrthographyBuilder builder = builderStack.pop();
        if(builder.size() > 0) {
            final PhoneticGroup phoneticGroupNew = new PhoneticGroup(builder.getElements());
            builderStack.peek().append(phoneticGroupNew);
        }
    }

    public Orthography getUpdatedOrthography() {
        return builderStack.peek().toOrthography();
    }

    @Override
    public void fallbackVisit(OrthographyElement obj) {
        builderStack.peek().append(obj);
    }

}
