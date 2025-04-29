package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Generate a new word intervals tier from an orthography tier using the provided
 * internal media list.  The number of words in the orthography tier should match
 * the number of internal media objects in the list.  The internal media objects
 * will be added to the new word intervals tier in the order they are provided.
 */
public class WorTierUpdateVisitor extends VisitorAdapter<OrthographyElement> {

    final Stack<OrthographyBuilder> builderStack = new Stack<>();

    final List<InternalMedia> internalMediaList;

    Iterator<InternalMedia> internalMediaItr;

    public WorTierUpdateVisitor(List<InternalMedia> internalMediaList) {
        super();
        this.internalMediaList = internalMediaList;
        reset();
    }

    public void reset() {
        builderStack.clear();
        builderStack.push(new OrthographyBuilder());
        internalMediaItr = internalMediaList.iterator();
    }

    @Visits
    public void visitOrthoGroup(OrthoGroup group) {
        builderStack.push(new OrthographyBuilder());
        for(OrthographyElement element : group.getElements()) {
            super.visit(element);
        }
        final OrthographyBuilder groupBuilder = builderStack.pop();
        if(groupBuilder.size() > 0) {
            OrthoGroup grp = new OrthoGroup(groupBuilder.getElements(), group.getAnnotations());
            builderStack.peek().append(grp);
        }
    }

    @Visits
    public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
        builderStack.push(new OrthographyBuilder());
        for(OrthographyElement element : phoneticGroup.getElements()) {
            super.visit(element);
        }
        final OrthographyBuilder groupBuilder = builderStack.pop();
        if(groupBuilder.size() > 0) {
            PhoneticGroup grp = new PhoneticGroup(groupBuilder.getElements());
            builderStack.peek().append(grp);
        }
    }

    @Visits
    public void visitWord(Word word) {
        builderStack.peek().append(word);
        if(internalMediaItr.hasNext()) {
            InternalMedia internalMedia = internalMediaItr.next();
            builderStack.peek().append(internalMedia);
        }
    }

    @Visits
    public void visitCompoundWord(CompoundWord compoundWord) {
        builderStack.peek().append(compoundWord);
        if(internalMediaItr.hasNext()) {
            InternalMedia internalMedia = internalMediaItr.next();
            builderStack.peek().append(internalMedia);
        }
    }

    @Visits
    public void visitInternalMedia(InternalMedia internalMedia) {
        // do not add
    }

    @Override
    public void fallbackVisit(OrthographyElement obj) {
        builderStack.peek().append(obj);
    }

    /**
     * Get the updated orthography
     *
     * @return the updated orthography or null if no orthography (should not happen)
     */
    public Orthography getOrthography() {
        if(builderStack.isEmpty()) {
            return null;
        }
        return builderStack.peek().toOrthography();
    }

}
