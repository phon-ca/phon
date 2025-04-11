package ca.phon.app.session.timeline;

import ca.phon.orthography.*;
import ca.phon.visitor.annotation.Visits;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Visitor for updating orthography intervals.  This visitor will create a new orthography
 * object with the provided intervals replacing the old ones.
 */
public class OrthoIntervalUpdateVisitor extends AbstractOrthographyVisitor {

    private Stack<OrthographyBuilder> builderStack = new Stack<>();

    private final List<InternalMedia> updatedIntervals;

    private Iterator<InternalMedia> updatedIntervalsIter = null;

    public OrthoIntervalUpdateVisitor(List<InternalMedia> updatedIntervals) {
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
    @Override
    public void visitInternalMedia(InternalMedia internalMedia) {
        if(updatedIntervalsIter.hasNext()) {
            InternalMedia newInterval = updatedIntervalsIter.next();
            builderStack.peek().append(newInterval);
        } else {
            builderStack.peek().append(internalMedia);
        }
    }

    @Visits
    @Override
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
    @Override
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

    public void getUpdatedOrthography() {
        builderStack.peek().toOrthography();
    }

    @Override
    public void fallbackVisit(OrthographyElement obj) {
        builderStack.peek().append(obj);
    }

}
